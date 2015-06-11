/******************************************************
	Cours :           LOG730
	Session :         �t� 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date cr�ation :   2010-05-21
******************************************************
Classe qui g�re la transmission et la r�ception
d'�v�nements du c�t� d'une instance d'Application.

La classe est en constante attente de nouveaux �v�nements
� l'aide d'un Thread. Lorsque l'Application associ�e
au Connector lui envoie un �v�nement, le Connector
envoie l'�v�nement au bus � l'aide d'un second Thread.
******************************************************/ 
package application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

import events.EventThatShouldBeSynchronized;
import events.IEvent;
import events.IPartOneEvent;

//nos événements ont une consciences écologiques, ils utilisent un BUS !!!!!!!!
public class EventBusConnector extends Thread implements IEventBusConnector {
	// Liste des �v�nements � �couter.
	@SuppressWarnings("unchecked")
	private List<Class> listenedEvents;
	
	private List<IEvent> lstEventsToSend = new ArrayList<IEvent>();
	private List<IObserver> lstObserver = new ArrayList<IObserver>();
	private Socket s;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private ReadEventFromStream readStream;
	
	@SuppressWarnings("unchecked")



	public EventBusConnector(List<Class> listenedEvents, String ip, int port,Class triggerClass, Integer order) {
		this.listenedEvents = listenedEvents;

		try {
			s = new Socket(ip, port);
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
			readStream = new ReadEventFromStream(ois, this,triggerClass,order);
		}
		catch(IOException ioe) {
			System.out.println("Impossible de se connecter au serveur.");
			System.exit(1);
		}
	}


	public EventBusConnector(List<Class> listenedEvents, String ip, int port) {
		this.listenedEvents = listenedEvents;

		try {
			s = new Socket(ip, port);
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
			readStream = new ReadEventFromStream(ois, this);
		}
		catch(IOException ioe) {
			System.out.println("Impossible de se connecter au serveur.");
			System.exit(1);
		}
	}
	
	public void start() {
		super.start();
		readStream.start();
	}
	
	//Thread qui envoie au bus d'�v�nements les �v�nements g�n�r�s par
	//son application.
	public void run()
	{
		while(true) {
			//Offrir une petite pause � l'application; un syst�me � �v�nement n'a pas besoin
			//de r�actions imm�diates
			try {
				//Offrir une pause au thread
				Thread.sleep(1000);
				
				synchronized(lstEventsToSend) {
					if(lstEventsToSend.size() > 0) {
						IEvent ie = lstEventsToSend.get(0);
						System.out.println("Envoie de l'�v�nement " + ie.toString());
						oos.writeObject(ie);
						lstEventsToSend.remove(0);
					}
				}

			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Compare un �v�nement avec la liste d'�v�nements � �couter.
	 */
	@SuppressWarnings("unchecked")
	public boolean listensToEvent(Object o)
	{
		boolean listens = false;
		for(int i = 0; i < listenedEvents.size() && !listens; i++)
		{
			Class c = listenedEvents.get(i);
			listens = (c.isInstance(o));
		}
		System.out.println("R�ception de l'�v�nement " + o.toString() + (listens?" trait�":"ignor�"));
		return listens;
	}
	
	public void callEvent(IEvent ie) {
		lstEventsToSend.add(ie);
	}
	
	public void notifyObservers(IEvent event)
	{
		for(IObserver o : lstObserver) {

			o.update(this, event);

		}
	}
	
	public void addObserver(IObserver o) 
	{
		lstObserver.add(o);
	}
}

//Thread qui �coute les �v�nements provenant du bus d'�v�nements.
//Le Connector achemine les �v�nements qui correspondent aux types � �couter
//dans listenedEvenets.
class ReadEventFromStream extends Thread {
	private ObjectInputStream ois;
	private EventBusConnector eventBusConn;
	private boolean firstInTheChain=false;
	private Class synchronizeTriggerCLass=null;
	private Integer orderInChain;
//liste d'evenement synchroniser en attente d'être affiché
	private Queue<IEvent> eventReceivedStack=new LinkedList<IEvent>();
	public ReadEventFromStream(ObjectInputStream ois, EventBusConnector eventBusConn, Class synchronizeTriggerCLass,Integer orderInChain) {
		this.ois = ois;
		this.eventBusConn = eventBusConn;
		this.synchronizeTriggerCLass=synchronizeTriggerCLass;
		this.orderInChain=orderInChain-1;
	}
	public ReadEventFromStream(ObjectInputStream ois, EventBusConnector eventBusConn) {
		this.ois = ois;
		this.eventBusConn = eventBusConn;
		this.firstInTheChain=true;
	}
	
	public void run() {
		while(true) {
			try {
				Object o = ois.readObject();
				// Les �v�nements re�us qui ne correspondent pas � ces types sont ignor�s par
				// le Connector.
				//ajouter o à la pile d'evenement

				if (eventBusConn.listensToEvent(o))

				/**
				 * Si l'événement recu est un événement synchronisé  et que le serveur n'est pas le pemier de la liste alors
				 * on ajoute l'événement dans la liste. On crée un timer qui va ce réveiller après un certain temps et traiter l'événement
				 * (On affiche le message à l'écran)
				 * . Si l'événement  a été retiré de la liste rien n' est fait.
				 *
				 */

					if(EventThatShouldBeSynchronized.class.isInstance(o)&&!firstInTheChain){
												eventReceivedStack.add((IEvent) o);
						((EventThatShouldBeSynchronized) o).setTimeAdded(new Date());
						new java.util.Timer().schedule(
								new java.util.TimerTask() {
									@Override
									public void run() {
									Date currentTime=new Date();
										Iterator<IEvent> listIterator=eventReceivedStack.iterator();
										int index=0;

										while (listIterator.hasNext()){
											EventThatShouldBeSynchronized eventInqueu=(EventThatShouldBeSynchronized)listIterator.next();
											/**
											 * un timer va ce réveiller et valider si l'événement a déjà été traité
											 */
											if(currentTime.getTime()-eventInqueu.getTimeAdded().getTime()>15000*orderInChain-1000){
												eventBusConn.notifyObservers( eventInqueu);
												eventReceivedStack.remove(eventInqueu);
												System.out.print("Une application dans la chaine n'a pas répondu");
											}
											index++;
										}
									}
								},
								(15000*orderInChain)
						);

					}
					else if(synchronizeTriggerCLass!=null&&synchronizeTriggerCLass.isInstance(o)){
						//Lorsqu'un événement de confirmation (update) est recu on l'affiche
						eventBusConn.notifyObservers((IEvent) eventReceivedStack.remove());
					}else{
						eventBusConn.notifyObservers((IEvent)o);
					}



			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}

/******************************************************
	Cours :           LOG730
	Session :         �t� 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date cr�ation :   2010-05-21
******************************************************
�v�nement lanc� par les boutons "Envoi Synchronis�"
des Applications.

******************************************************/ 
package events;

import java.util.Date;

public class EventThatShouldBeSynchronized extends EventForAll implements IEventSynchronized {

	private static final long serialVersionUID = 6603201529319860113L;

	public Date getTimeAdded() {
		return timeAdded;
	}

	public void setTimeAdded(Date timeAdded) {
		this.timeAdded = timeAdded;
	}

	private Date timeAdded;
	public EventThatShouldBeSynchronized(String m){
		super(m);
	}

}

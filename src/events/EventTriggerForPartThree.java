/******************************************************
	Cours :           LOG730
	Session :         �t� 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date cr�ation :   2010-05-21
******************************************************
�v�nement lanc� par les boutons "Envoyer � App Un"
des Applications.
******************************************************/ 
package events;

public class EventTriggerForPartThree extends EventBase implements IAckEvent{


	private static final long serialVersionUID = -2908700414599347327L;

	public EventTriggerForPartThree(String m){
		super(m);
	}
}

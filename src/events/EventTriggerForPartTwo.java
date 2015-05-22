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

public class EventTriggerForPartTwo extends EventBase implements IAckEvent{


	private static final long serialVersionUID = -1658999729840687338L;

	public EventTriggerForPartTwo(String m){
		super(m);
	}
}

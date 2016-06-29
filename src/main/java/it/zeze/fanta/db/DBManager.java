package it.zeze.fanta.db;

import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateful
@LocalBean
public class DBManager {
	
	@PersistenceContext(unitName = "FantaFormazioneService")
	EntityManager em;
	
	public EntityManager getEm() {
		return em;
	}

	public void persist(Object toPersist){
		em.persist(toPersist);
		em.flush();
	}
}

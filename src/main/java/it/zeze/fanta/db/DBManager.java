package it.zeze.fanta.db;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import it.zeze.fantaformazioneweb.entity.Giornate;
import it.zeze.fantaformazioneweb.entity.Squadre;

@Stateful
@LocalBean
@SuppressWarnings("unchecked")
public class DBManager {
	
	@PersistenceContext(unitName = "FantaFormazioneService")
	EntityManager em;
	
	public EntityManager getEm() {
		return em;
	}

	public void persist(Object toPersist){
		em.persist(toPersist);
	}
	
	public Giornate getGiornate(int numeroGiornata, String stagione){
		Giornate toReturn = null;
		String qryString = "SELECT g FROM Giornate g WHERE g.numeroGiornata = :numeroGiornata AND g.stagione = :stagione";
		Query query = em.createQuery(qryString);
		query.setParameter("numeroGiornata", numeroGiornata);
		query.setParameter("stagione", stagione);
		toReturn = (Giornate) query.getSingleResult();
		return toReturn;
	}
	
	public List<Squadre> getSquadreAll(){
		List<Squadre> toReturn = new ArrayList<Squadre>();
		String qryString = "SELECT s FROM Squadre s";
		Query query = em.createQuery(qryString);
		toReturn = query.getResultList();
		return toReturn;
	}
}

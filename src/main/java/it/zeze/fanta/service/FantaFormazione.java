package it.zeze.fanta.service;

import it.zeze.fantaformazioneweb.entity.Giornate;

import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/fantaWs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class FantaFormazione {

	@PersistenceContext(unitName = "FantaFormazioneService")
	EntityManager em;

	@GET
	@Path("/giornate")
	public Giornate getGiornata() throws NamingException {
		return (Giornate) em.createQuery("SELECT g FROM Giornate g").getResultList().get(0);
	}
}

package it.zeze.fanta.service.ejb.proxy.seam;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.definition.ejb.FormazioniGazzettaLocal;
import it.zeze.fanta.service.definition.ejb.proxy.seam.FormazioniGazzettaSeamRemote;
import it.zeze.fantaformazioneweb.entity.ProbabiliFormazioniGazzetta;

@Stateless
@LocalBean
public class FormazioniGazzettaSeamEJB implements FormazioniGazzettaSeamRemote {

	private static final Logger log = LogManager.getLogger(FormazioniGazzettaSeamEJB.class);

	@EJB(name = "FormazioniGazzettaEJB")
	private FormazioniGazzettaLocal formazioniGazzettaLocal;

	@Override
	public void unmarshallAndSaveFromHtmlFile(String stagione) {
		formazioniGazzettaLocal.unmarshallAndSaveFromHtmlFile(stagione);
	}
	
	@Override
	public ProbabiliFormazioniGazzetta selectByIdGiocatoreIdGiornata(int idGiocatore, int idGiornata) {
		return formazioniGazzettaLocal.selectByIdGiocatoreIdGiornata(idGiocatore, idGiornata);
	}
}

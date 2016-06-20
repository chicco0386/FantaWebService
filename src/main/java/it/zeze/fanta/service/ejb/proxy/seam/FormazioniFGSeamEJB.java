package it.zeze.fanta.service.ejb.proxy.seam;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.definition.ejb.FormazioniFGLocal;
import it.zeze.fanta.service.definition.ejb.proxy.seam.FormazioniFGSeamRemote;
import it.zeze.fantaformazioneweb.entity.ProbabiliFormazioniFg;

@Stateless
@LocalBean
public class FormazioniFGSeamEJB implements FormazioniFGSeamRemote {
	
	private static final Logger log = LogManager.getLogger(FormazioniFGSeamEJB.class);
		
	@EJB(name = "FormazioniFGEJB")
	private FormazioniFGLocal formazioniFGLocal;

	@Override
	public void unmarshallAndSaveFromHtmlFile(String stagione) {
		formazioniFGLocal.unmarshallAndSaveFromHtmlFile(stagione);
	}
	
	@Override
	public ProbabiliFormazioniFg selectByIdGiocatoreIdGiornata(int idGiocatore, int idGiornata) {
		return formazioniFGLocal.selectByIdGiocatoreIdGiornata(idGiocatore, idGiornata);
	}
	
}

package it.zeze.fanta.service.ejb.proxy.seam;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.definition.ejb.SquadreLocal;
import it.zeze.fanta.service.definition.ejb.proxy.seam.SquadreSeamRemote;
import it.zeze.fantaformazioneweb.entity.Squadre;

@Stateless
@LocalBean
public class SquadreSeamEJB implements SquadreSeamRemote {

	private static final Logger log = LogManager.getLogger(SquadreSeamEJB.class);

	@EJB(name = "SquadreEJB")
	private SquadreLocal squadreEJB;

	@Override
	public void unmarshallAndSaveFromHtmlFile() {
		squadreEJB.unmarshallAndSaveFromHtmlFile();
	}
	
	@Override
	public void initMappaSquadre() {
		squadreEJB.initMappaSquadre();
	}

	@Override
	public Squadre getSquadraFromMapByNome(String nomeSquadraToSearch) {
		return squadreEJB.getSquadraFromMapByNome(nomeSquadraToSearch);
	}

	@Override
	public Squadre getSquadraByNome(String nomeSquadraToSearch) {
		return squadreEJB.getSquadraByNome(nomeSquadraToSearch);
	}

	@Override
	public Squadre getSquadraById(int idSquadra) {
		return squadreEJB.getSquadraById(idSquadra);
	}
}

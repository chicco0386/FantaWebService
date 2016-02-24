package it.zeze.fanta.service.ejb;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import it.zeze.fanta.db.DBManager;
import it.zeze.fanta.service.definition.ejb.GiocatoriLocal;
import it.zeze.fanta.service.definition.ejb.GiocatoriRemote;

@Stateless
@LocalBean
public class GiocatoriEJB implements GiocatoriLocal, GiocatoriRemote {

	@EJB(name = "DBManager")
	private DBManager dbManager;

	@Override
	public void unmarshallAndSaveFromHtmlFile(boolean noLike) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unmarshallAndSaveFromHtmlFileForUpdateStagione(boolean noLike) {
		// TODO Auto-generated method stub

	}
}

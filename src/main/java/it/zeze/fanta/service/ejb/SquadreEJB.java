package it.zeze.fanta.service.ejb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import it.zeze.fanta.db.DBManager;
import it.zeze.fanta.service.definition.ejb.SquadreLocal;
import it.zeze.fanta.service.definition.ejb.SquadreRemote;
import it.zeze.fantaformazioneweb.entity.Squadre;

@Stateless
@LocalBean
public class SquadreEJB implements SquadreLocal, SquadreRemote {
	
	@EJB(name = "DBManager")
	private DBManager dbManager;
	
	private Map<Integer, String> mapSquadre = new HashMap<Integer, String>();

	@Override
	public void unmarshallAndSaveFromHtmlFile() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initMappaSquadre() {
		mapSquadre.clear();
		List<Squadre> result = dbManager.getSquadreAll();
		for (int i = 0; i < result.size(); i++) {
			mapSquadre.put(result.get(i).getId(), result.get(i).getNome());
		}
	}

	@Override
	public Squadre getSquadraFromMapByNome(String nomeSquadraToSearch) {
		Squadre squadraToReturn = new Squadre();
		if (mapSquadre == null || mapSquadre.isEmpty()) {
			initMappaSquadre();
		}
		Iterator<Entry<Integer, String>> it = mapSquadre.entrySet().iterator();
		boolean trovato = false;
		Entry<Integer, String> currentEntity;
		while (it.hasNext() && !trovato) {
			currentEntity = it.next();
			if (currentEntity.getValue().equalsIgnoreCase(nomeSquadraToSearch.trim())) {
				trovato = true;
				squadraToReturn.setId(currentEntity.getKey());
				squadraToReturn.setNome(currentEntity.getValue());
			}
		}
		return squadraToReturn;
	}

	@Override
	public Squadre getSquadraByNome(String nomeSquadraToSearch) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Squadre getSquadraById(int idSquadra) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Squadre> getSquadre() {
		return null;
		// TODO Auto-generated method stub
		
	}

}

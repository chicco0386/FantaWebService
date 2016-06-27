package it.zeze.fanta.service.ejb.proxy.seam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import it.zeze.fanta.service.bean.GiocatoriMercato;
import it.zeze.fanta.service.definition.ejb.FormazioneMercatoLocal;
import it.zeze.fanta.service.definition.ejb.proxy.seam.FormazioneMercatoSeamRemote;
import it.zeze.fantaformazioneweb.entity.Giocatori;
import it.zeze.fantaformazioneweb.entity.wrapper.GiocatoriWrap;

@Stateless
@LocalBean
public class FormazioneMercatoSeamEJB implements FormazioneMercatoSeamRemote {

	@EJB(name = "FormazioneMercatoEJB")
	private FormazioneMercatoLocal formazioniMercatoEJB;

	@Override
	public void initCreditiResidui(int idUtentiFormazioni, int idUtente) {
		formazioniMercatoEJB.initCreditiResidui(idUtentiFormazioni, idUtente);
		
	}

	@Override
	public void remove(GiocatoriWrap giocatoreToRemove) {
		formazioniMercatoEJB.remove(giocatoreToRemove.unwrap());
		
	}

	@Override
	public void removeMercato(GiocatoriMercato giocatoreToRemove, BigDecimal prezzo) {
		formazioniMercatoEJB.removeMercato(giocatoreToRemove, prezzo);
		
	}

	@Override
	public void setCreditiResidui(BigDecimal creditiResidui) {
		formazioniMercatoEJB.setCreditiResidui(creditiResidui);
	}

	@Override
	public BigDecimal getCreditiResidui() {
		return formazioniMercatoEJB.getCreditiResidui();
	}

	@Override
	public void setPrezzoAcquisto(BigDecimal prezzoAcquisto) {
		formazioniMercatoEJB.setPrezzoAcquisto(prezzoAcquisto);
	}

	@Override
	public BigDecimal getPrezzoAcquisto() {
		return formazioniMercatoEJB.getPrezzoAcquisto();
	}

	@Override
	public void setListaGiocatoriMercato(List<GiocatoriMercato> listaGiocatoriMercato) {
		formazioniMercatoEJB.setListaGiocatoriMercato(listaGiocatoriMercato);
		
	}

	@Override
	public List<GiocatoriMercato> getListaGiocatoriMercato() {
		return formazioniMercatoEJB.getListaGiocatoriMercato();
	}

	@Override
	public void setIdUtenteFormazioneToInit(int idUtenteFormazioneToInit) {
		formazioniMercatoEJB.setIdUtenteFormazioneToInit(idUtenteFormazioneToInit);
	}

	@Override
	public void setDoInitCrediti(boolean doInitCrediti) {
		formazioniMercatoEJB.setDoInitCrediti(doInitCrediti);
	}

	@Override
	public boolean isDoInitCrediti() {
		return formazioniMercatoEJB.isDoInitCrediti(); 
	}

	@Override
	public void setDoInit(boolean doInit) {
		formazioniMercatoEJB.setDoInit(doInit);
	}

	@Override
	public boolean isDoInit() {
		return formazioniMercatoEJB.isDoInit();
	}

	@Override
	public int getIdUtenteFormazioneToInit() {
		return formazioniMercatoEJB.getIdUtenteFormazioneToInit();
	}

	@Override
	public void setListaGiocatori(List<GiocatoriWrap> listaGiocatori) {
		List<Giocatori> toPass = new ArrayList<Giocatori>();
		for (GiocatoriWrap current : listaGiocatori){
			toPass.add(current.unwrap());
		}
		formazioniMercatoEJB.setListaGiocatori(toPass);
	}

	@Override
	public List<GiocatoriWrap> getListaGiocatori() {
		List<GiocatoriWrap> toReturn = new ArrayList<GiocatoriWrap>();
		List<Giocatori> ejbResp = formazioniMercatoEJB.getListaGiocatori();
		for (Giocatori current : ejbResp){
			toReturn.add(new GiocatoriWrap(current));
		}
		return toReturn;
	}

	@Override
	public void add(GiocatoriWrap giocatoreToInsert) {
		formazioniMercatoEJB.add(giocatoreToInsert.unwrap());
	}

	@Override
	public void addGiocatoreMercato(GiocatoriWrap giocatoreToInsert, BigDecimal prezzoAcquisto) {
		formazioniMercatoEJB.addGiocatoreMercato(giocatoreToInsert.unwrap(), prezzoAcquisto);		
	}

	@Override
	public void initListaGiocatori(int idUtente) {
		formazioniMercatoEJB.initListaGiocatori(idUtente);
	}

	@Override
	public void initListaGiocatoriMercato(int idUtente) {
		formazioniMercatoEJB.initListaGiocatoriMercato(idUtente);		
	}
	
	
}
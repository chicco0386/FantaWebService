package it.zeze.fanta.service.ejb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.bean.GiocatoriMercato;
import it.zeze.fanta.service.definition.ejb.FormazioneMercatoLocal;
import it.zeze.fanta.service.definition.ejb.FormazioneMercatoRemote;
import it.zeze.fanta.service.definition.ejb.FormazioniLocal;
import it.zeze.fanta.service.definition.ejb.UtentiFormazioniLocal;
import it.zeze.fantaformazioneweb.entity.Formazioni;
import it.zeze.fantaformazioneweb.entity.Giocatori;
import it.zeze.fantaformazioneweb.entity.UtentiFormazioni;
import it.zeze.util.GiocatoriComparator;

@Stateless
@LocalBean
public class FormazioneMercatoEJB implements FormazioneMercatoLocal, FormazioneMercatoRemote {
	
	/*
	 * Classe FormazioniBea in SEAM
	 */

	private static final Logger log = LogManager.getLogger(CalendarioEJB.class);

	@EJB(name="UtentiFormazioniEJB")
	private UtentiFormazioniLocal utentiFormazioniEJB;

	@EJB(name="FormazioniEJB")
	private FormazioniLocal formazioniEJB;

	private List<Giocatori> listaGiocatori = new ArrayList<Giocatori>();

	private boolean doInit = true;
	private boolean doInitCrediti = true;

	private int idUtenteFormazioneToInit = -1;

	private List<GiocatoriMercato> listaGiocatoriMercato = new ArrayList<GiocatoriMercato>();
	private BigDecimal prezzoAcquisto = BigDecimal.ZERO;
	private BigDecimal creditiResidui = BigDecimal.ZERO;

	@Override
	public void initListaGiocatoriMercato(int idUtente) {
		log.info("Init listaGiocatoriMercato doInit [" + doInit + "]");
		if (idUtenteFormazioneToInit != -1) {
			if (doInit) {
				if (utentiFormazioniEJB.esisteUtentiFormazioni(idUtenteFormazioneToInit, idUtente)) {
					listaGiocatoriMercato.clear();
					List<Formazioni> formazioni = formazioniEJB.selectFormazioniByIdUtenteFormazioni(idUtenteFormazioneToInit);
					for (int i = 0; i < formazioni.size(); i++) {
						GiocatoriMercato giocatoreMercato = new GiocatoriMercato(formazioni.get(i).getGiocatori());
						if (formazioni.get(i).getPrezzoAcquisto() == null) {
							giocatoreMercato.setPrezzoAcquisto(BigDecimal.ZERO);
						} else {
							giocatoreMercato.setPrezzoAcquisto(formazioni.get(i).getPrezzoAcquisto());
						}
						listaGiocatoriMercato.add(giocatoreMercato);
					}
					// lo setto in modo da nn rifare l'init se eseguo un action
					// ajax sulla pagina
					doInit = false;
				}
			}
		}
		Collections.sort(listaGiocatoriMercato, Collections.reverseOrder(new GiocatoriComparator()));
	}

	@Override
	public void initListaGiocatori(int idUtente) {
		log.info("Init listaGiocatori doInit [" + doInit + "]");
		if (idUtenteFormazioneToInit != -1) {
			if (doInit) {
				if (utentiFormazioniEJB.esisteUtentiFormazioni(idUtenteFormazioneToInit, idUtente)) {
					listaGiocatori.clear();
					List<Formazioni> formazioni = formazioniEJB.selectFormazioniByIdUtenteFormazioni(idUtenteFormazioneToInit);
					for (int i = 0; i < formazioni.size(); i++) {
						listaGiocatori.add(formazioni.get(i).getGiocatori());
					}
					// lo setto in modo da nn rifare l'init se eseguo un action
					// ajax sulla pagina
					doInit = false;
				}
			}
		}
		Collections.sort(listaGiocatori, Collections.reverseOrder(new GiocatoriComparator()));
	}

	@Override
	public void addGiocatoreMercato(Giocatori giocatoreToInsert, BigDecimal prezzoAcquisto) {
		if (!listaGiocatoriMercato.contains(giocatoreToInsert)) {
			log.info("Prezzo acquisto: [" + prezzoAcquisto.toPlainString() + "]");
			GiocatoriMercato mercato = new GiocatoriMercato(giocatoreToInsert);
			mercato.setPrezzoAcquisto(prezzoAcquisto);
			listaGiocatoriMercato.add(mercato);
			creditiResidui = creditiResidui.subtract(mercato.getPrezzoAcquisto());
			this.prezzoAcquisto = BigDecimal.ZERO;
		}
	}

	@Override
	public void add(Giocatori giocatoreToInsert) {
		if (!listaGiocatori.contains(giocatoreToInsert)) {
			listaGiocatori.add(giocatoreToInsert);
		}
	}

	@Override
	public List<Giocatori> getListaGiocatori() {
		return listaGiocatori;
	}

	@Override
	public void setListaGiocatori(List<Giocatori> listaGiocatori) {
		this.listaGiocatori = listaGiocatori;
	}

	@Override
	public int getIdUtenteFormazioneToInit() {
		return idUtenteFormazioneToInit;
	}

	@Override
	public boolean isDoInit() {
		return doInit;
	}

	@Override
	public void setDoInit(boolean doInit) {
		this.doInit = doInit;
	}

	@Override
	public boolean isDoInitCrediti() {
		return doInitCrediti;
	}

	@Override
	public void setDoInitCrediti(boolean doInitCrediti) {
		this.doInitCrediti = doInitCrediti;
	}

	@Override
	public void setIdUtenteFormazioneToInit(int idUtenteFormazioneToInit) {
		this.idUtenteFormazioneToInit = idUtenteFormazioneToInit;
	}

	@Override
	public List<GiocatoriMercato> getListaGiocatoriMercato() {
		return listaGiocatoriMercato;
	}

	@Override
	public void setListaGiocatoriMercato(List<GiocatoriMercato> listaGiocatoriMercato) {
		this.listaGiocatoriMercato = listaGiocatoriMercato;
	}

	@Override
	public BigDecimal getPrezzoAcquisto() {
		return prezzoAcquisto;
	}

	@Override
	public void setPrezzoAcquisto(BigDecimal prezzoAcquisto) {
		this.prezzoAcquisto = prezzoAcquisto;
	}

	@Override
	public BigDecimal getCreditiResidui() {
		return creditiResidui;
	}

	@Override
	public void setCreditiResidui(BigDecimal creditiResidui) {
		this.creditiResidui = creditiResidui;
	}

	@Override
	public void removeMercato(GiocatoriMercato giocatoreToRemove, BigDecimal prezzo) {
		listaGiocatoriMercato.remove(giocatoreToRemove);
		creditiResidui = creditiResidui.add(prezzo);
	}

	@Override
	public void remove(Giocatori giocatoreToRemove) {
		listaGiocatori.remove(giocatoreToRemove);
	}

	@Override
	public void initCreditiResidui(int idUtentiFormazioni, int idUtente) {
		log.info("initCreditiResidui doInitCrediti [" + doInitCrediti + "]");
		UtentiFormazioni utForm = utentiFormazioniEJB.getUtentiFormazioniByIdAndIdUtente(idUtentiFormazioni, idUtente);
		if (utForm.getCrediti() != null) {
			this.creditiResidui = utForm.getCrediti();
		} else {
			this.creditiResidui = BigDecimal.ZERO;
		}
		this.doInitCrediti = false;
	}
}

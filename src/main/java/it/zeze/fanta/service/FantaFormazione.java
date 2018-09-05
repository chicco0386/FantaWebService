package it.zeze.fanta.service;

import it.zeze.fanta.ejb.util.JNDIUtils;
import it.zeze.fanta.service.definition.ejb.proxy.seam.CalendarioSeamRemote;
import it.zeze.fantaformazioneweb.entity.wrapper.GiornateWrap;
import it.zeze.util.FantaFormazioneUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/fantaWs")
public class FantaFormazione {

    private static final Logger log = LogManager.getLogger(FantaFormazione.class);

    private static CalendarioSeamRemote calendarioEJB;

    static {
        try {
            calendarioEJB = JNDIUtils.getCalendarioEJB();
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @GET
    @Path("/echo")
    public String echo() throws NamingException {
        return "HELLO WORLD !!!";
    }

    @GET
    @Path("/giornate")
    @Produces(MediaType.APPLICATION_JSON)
    public GiornateWrap getGiornata() throws NamingException {
        GiornateWrap calendario = null;
        try {
            calendario = calendarioEJB.getGiornate();
            System.out.println(calendario.getNumeroGiornata());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calendario;
    }

    @GET
    @Path("/statisticheDownload")
    public void statisticheDownload() throws Exception {
        String rootHTMLFiles = "/home/enrico/Desktop/";
        String nomeFileSquadre = "statisticheG{giornata}.html";
        String pathFileHTMLStatistiche = FilenameUtils.concat(rootHTMLFiles, nomeFileSquadre);
        FantaFormazioneUtil.salvaStatistichePerTutteLeGiornateNew(pathFileHTMLStatistiche);
    }
}

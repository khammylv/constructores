package com.semillero.Constructores.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TareasProgramadasService {

      

    @Autowired
    private  OrdenConstruccionService ordenService;

    /** para probar ejecucion */
 //@Scheduled(fixedDelay = 10000)
    //  Ejecuta todos los días a las 7:00 AM
    //@Scheduled(cron = "0 0 7 * * *")
    //@Scheduled(fixedDelay = 10000)
    @Scheduled(cron = "0 0 7 * * *")
    public void programarOrdenDiaria() {
        System.out.println("Ejecutando tarea programada: programarOrdenDiariaDia()");
        ordenService.programarSiguienteOrdenAsync()
                .thenAccept(opt -> opt.ifPresent(orden ->
                        System.out.println("✅ Orden programada automáticamente: " + orden.getId())
                ));
    }

    //  Ejecuta todos los días a las 8:00 PM
    //
    //@Scheduled(fixedDelay = 10000)
   //@Scheduled(cron = "0 0 20 * * *")
   @Scheduled(cron = "0 0 20 * * *")
    public void finalizarOrdenDiaria() {
        System.out.println(" Ejecutando tarea programada: finalizarOrdenDiariaNoche()");
        ordenService.finalizarOrdenSiCorrespondeAsync()
                .thenAccept(opt -> opt.ifPresent(orden ->
                        System.out.println("✅ Orden finalizada automáticamente: " + orden.getId())
                ));
    }

}

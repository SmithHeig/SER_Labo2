package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.*;
import views.*;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class ControleurMedia {

	private ControleurGeneral ctrGeneral;
	private static MainGUI mainGUI;
	private ORMAccess ormAccess;
	
	private GlobalData globalData;

	public ControleurMedia(ControleurGeneral ctrGeneral, MainGUI mainGUI, ORMAccess ormAccess){
		this.ctrGeneral=ctrGeneral;
		ControleurMedia.mainGUI=mainGUI;
		this.ormAccess=ormAccess;
	}

	protected class ProjectionsJSON{
		private List<ProjectionJSON> projections;

		protected class ProjectionJSON{
			private String date;
			private String titre;
			private String premierRole;
			private String deuxiemeRole;

			protected ProjectionJSON(Projection projection){
				// DATE
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				this.date =dateFormat.format(projection.getDateHeure().getTime());
				// TITRE
				this.titre = projection.getFilm().getTitre();

				Set<RoleActeur> roles = projection.getFilm().getRoles();
				Iterator it = roles.iterator();

				// PREMIER ROLE
				premierRole = ((RoleActeur) it.next()).getActeur().getNom();

				// DEUXIEME ROLE
				deuxiemeRole = ((RoleActeur) it.next()).getActeur().getNom();
				System.out.println(date + " " + titre + " " + premierRole + " " + deuxiemeRole);
			}

		}

		protected ProjectionsJSON(){}

		protected ProjectionsJSON(List<ProjectionJSON> projections){
			this.projections = projections;
		}

		protected void setProjections(List<ProjectionJSON> projections){
			this.projections = projections;
		}
	}

	public void sendJSONToMedia(){
		new Thread(){
			public void run(){
				mainGUI.setAcknoledgeMessage("Envoi JSON ... WAIT");
				long currentTime = System.currentTimeMillis();
				try {
					globalData = ormAccess.GET_GLOBAL_DATA();
					//mainGUI.setWarningMessage("Envoi JSON: Fonction non encore implementee");

					List<ProjectionsJSON.ProjectionJSON> projectionJSONList = new ArrayList<>();

					for(Projection projection: globalData.getProjections()){
						projectionJSONList.add(new ProjectionsJSON().new ProjectionJSON(projection));
					}
					ProjectionsJSON projectionsJSON = new ProjectionsJSON(projectionJSONList);

					try (PrintWriter writer = new PrintWriter(new FileWriter("cinema.json"))) {
						Gson gson = new GsonBuilder().setPrettyPrinting().create();
						gson.toJson(projectionsJSON, writer);
					}
					long endTime = System.currentTimeMillis();
					System.out.println("JSON done in " + timeToSeconds(currentTime, endTime) + "");
					mainGUI.setAcknoledgeMessage("Le fichier JSON a été créé en " + timeToSeconds(currentTime, endTime));
				}
				catch (Exception e){
					mainGUI.setErrorMessage("Construction JSON impossible", e.toString());
				}
			}
		}.start();
	}

	private String timeToSeconds(long start, long finish){
		long time = Math.abs(finish - start);
		double timeSeconds = (double)time / 1000;
		return String.valueOf(timeSeconds) + " s";

	}

}
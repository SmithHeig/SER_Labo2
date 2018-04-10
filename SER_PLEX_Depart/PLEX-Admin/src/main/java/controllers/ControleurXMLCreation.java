package controllers;

import models.*;
import views.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.EventListener;
import java.util.List;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom2.*;

/*import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
*/

import com.thoughtworks.xstream.XStream;

public class ControleurXMLCreation {

	//private ControleurGeneral ctrGeneral;
	private static MainGUI mainGUI;
	private ORMAccess ormAccess;

	private GlobalData globalData;

	public ControleurXMLCreation(ControleurGeneral ctrGeneral, MainGUI mainGUI, ORMAccess ormAccess){
		//this.ctrGeneral=ctrGeneral;
		ControleurXMLCreation.mainGUI=mainGUI;
		this.ormAccess=ormAccess;
	}

	public void createXML(){
		new Thread(){
				public void run(){
					mainGUI.setAcknoledgeMessage("Creation XML... WAIT");
					long currentTime = System.currentTimeMillis();
					try {
						globalData = ormAccess.GET_GLOBAL_DATA();

						DocType doctype = new DocType("cinema","cinema.dtd");

						Element root = new Element("projections");

						Document document = new Document(root, doctype);

						for(Projection projection: globalData.getProjections()){
							createProjection(projection,root);
						}


					}
					catch (Exception e){
						mainGUI.setErrorMessage("Construction XML impossible", e.toString());
					}
				}
		}.start();
	}

	private void createProjection(Projection projection, Element root){
		Element project = new Element("projection");

		/** ID Projection**/
		Element id = new Element("id");
		id.setText(Long.toString(projection.getId()));
		project.addContent(id);

		/** DATE **/
		Element date = createDate(projection.getDateHeure());
		project.addContent(date);

		/** SALLE **/
		Element salle = createSalle(projection.getSalle());
		project.addContent(salle);

		/** FILM **/
		Element film = createFilm(projection.getFilm());

	}

	public Element createDate(Calendar dateValue){
		Element date = new Element("date");

		Element jour = new Element("jour");
		Element mois = new Element("mois");
		Element annee = new Element("annee");

		int intJour = dateValue.get(Calendar.DAY_OF_MONTH);
		int intMois = dateValue.get(Calendar.MONTH);
		int intAnnee = dateValue.get(Calendar.YEAR);

		jour.setText(Integer.toString(intJour));
		mois.setText(Integer.toString(intMois));
		annee.setText(Integer.toString(intAnnee));

		date.addContent(jour);
		date.addContent(mois);
		date.addContent(annee);
		return date;
	}

	private Element createSalle(Salle salleValue){
		Element salle = new Element("salle");

		Element id = new Element("id");
		Element noSalle = new Element("noSalle");
		Element taille = new Element("taille");

		id.setText(Long.toString(salleValue.getId()));
		noSalle.setText(salleValue.getNo());
		taille.setText(Integer.toString(salleValue.getTaille()));

		salle.addContent(id);
		salle.addContent(noSalle);
		salle.addContent(taille);

		return salle;
	}

	private Element createFilm(Film filmValue){
		Element film = new Element("film");

		Element id = new Element("id");
		Element titre = new Element("titre");
		Element synopsis = new Element("synopsis");
		Element duree = new Element("durée");
		Element photo = new Element("photo");
		Element critiques = createCritiques(filmValue);
		Element motsCles = createMotsCles(filmValue);
		Element langues = createLangues(filmValue);
		Element roles = createRoles(filmValue);

		id.setText(Long.toString(filmValue.getId()));
		titre.setText(filmValue.getTitre());
		synopsis.setText(filmValue.getSynopsis());
		duree.setText(Integer.toString(filmValue.getDuree()));
		photo.setAttribute("url",filmValue.getPhoto());

		film.addContent(id);
		film.addContent(titre);
		film.addContent(synopsis);
		film.addContent(duree);
		film.addContent(critiques);
		film.addContent(motsCles);
		film.addContent(langues);
		film.addContent(photo);

		return film;
	}

	private Element createCritiques(Film filmValue){
		Element critiques = new Element("critiques");

		for(Critique critiqueValue: filmValue.getCritiques()){
			Element critique = new Element("critique");
			critique.setText(critiqueValue.getTexte());
			critiques.addContent(critique);
		}

		return critiques;
	}

	private Element createMotsCles(Film filmValue){
		Element motsCles = new Element("mots-cles");

		for(Motcle motcleValue: filmValue.getMotcles()){
			Element motcle = new Element("mot-cle");
			motcle.setText(motcleValue.getLabel());
			motsCles.addContent(motcle);
		}

		return motsCles;
	}

	private Element createLangues(Film filmValue){
		Element langues = new Element("langues");

		for(Langage langueValue : filmValue.getLangages()){
			Element langue = new Element("langue");
			langue.setText(langueValue.getLabel());
			langues.addContent(langue);
		}

		return langues;
	}

	private Element createRoles(Film filmValue){
		Element roles = new Element("roles");

		for(RoleActeur roleValue: filmValue.getRoles()){
			Element role = new Element("role");

			Element id = new Element("id");
			Element personnage = new Element("personnage");
			Element place = new Element("place");
			Element acteur = createActeur(roleValue);

			id.setText(Long.toString(roleValue.getId()));
			personnage.setText(roleValue.getPersonnage());
			place.setText(Long.toString(roleValue.getPlace()));

			role.addContent(id);
			role.addContent(personnage);
			role.addContent(place);
			role.addContent(acteur);
			role.setAttribute("sexe",roleValue.getActeur().getSexe().toString());

			roles.addContent(role);

		}

		return roles;
	}

	private Element createActeur(RoleActeur roleValue){
		Acteur acteurValue = roleValue.getActeur();
		Element acteur = new Element("acteur");

		Element nom = new Element("nom");
		Element nomNaissance = new Element("nomNaissance");
		Element biographie = new Element("biographie");

		Element dateNaissance = new Element("dateNaissance");
		dateNaissance.addContent(createDate(acteurValue.getDateNaissance()));
		Element dateDeces = new Element("dateDeces");
		dateDeces.addContent(createDate(acteurValue.getDateDeces()));

		acteur.addContent(nom);
		acteur.addContent(nomNaissance);
		acteur.addContent(biographie);
		acteur.addContent(dateNaissance);
		acteur.addContent(dateDeces);

		return acteur;
	}

	public void createXStreamXML(){
		new Thread(){
				public void run(){
					mainGUI.setAcknoledgeMessage("Creation XML... WAIT");
					long currentTime = System.currentTimeMillis();
					try {
						globalData = ormAccess.GET_GLOBAL_DATA();
						globalDataControle();
					}
					catch (Exception e){
						mainGUI.setErrorMessage("Construction XML impossible", e.toString());
					}

					XStream xstream = new XStream();
					writeToFile("global_data.xml", xstream, globalData);
					System.out.println("Done [" + displaySeconds(currentTime, System.currentTimeMillis()) + "]");
					mainGUI.setAcknoledgeMessage("XML cree en "+ displaySeconds(currentTime, System.currentTimeMillis()) );
				}
		}.start();
	}

	private static void writeToFile(String filename, XStream serializer, Object data) {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
			serializer.toXML(data, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static final DecimalFormat doubleFormat = new DecimalFormat("#.#");
	private static final String displaySeconds(long start, long end) {
		long diff = Math.abs(end - start);
		double seconds = ((double) diff) / 1000.0;
		return doubleFormat.format(seconds) + " s";
	}

	private void globalDataControle(){
		for (Projection p:globalData.getProjections()){
			System.out.println("******************************************");
			System.out.println(p.getFilm().getTitre());
			System.out.println(p.getSalle().getNo());
			System.out.println("Acteurs *********");
			for(RoleActeur role : p.getFilm().getRoles()) {
				System.out.println(role.getActeur().getNom());
			}
			System.out.println("Genres *********");
			for(Genre genre : p.getFilm().getGenres()) {
				System.out.println(genre.getLabel());
			}
			System.out.println("Mot-cles *********");
			for(Motcle motcle : p.getFilm().getMotcles()) {
				System.out.println(motcle.getLabel());
			}
			System.out.println("Langages *********");
			for(Langage langage : p.getFilm().getLangages()) {
				System.out.println(langage.getLabel());
			}
			System.out.println("Critiques *********");
			for(Critique critique : p.getFilm().getCritiques()) {
				System.out.println(critique.getNote());
				System.out.println(critique.getTexte());
			}
		}
	}
}




# SER - Rapport labo 2

## Introduction

Dans ce laboratoire, nous avons du implémenter des fonctions java pour transformer des données d'une base de données contenant des projections en *XML* et *JSON*.

## Changement XML - DTD

```dtd
<!ELEMENT projections (projection)*>
<!ELEMENT projection (id, date, salle, film)>
<!ELEMENT film (id, titre, synopsis, durée, (critiques*), (genres*), (mots-cles*), (langues*), photo?, (roles*))>
<!ELEMENT acteur (nom, nomNaissance?, biographie, dateNaissance?, dateDeces?)>
<!ELEMENT photo EMPTY>
<!ELEMENT date (jour,mois,annee)>
<!ELEMENT salle (id, noSalle, taille)>

<!ELEMENT critiques (critique*)>
<!ELEMENT genres (genre*)>
<!ELEMENT mots-cles (mot-cle*)>
<!ELEMENT langues (langue*)>
<!ELEMENT roles (role*)>
<!ELEMENT role (id,personnage, place, acteur)>
<!ELEMENT id (#PCDATA)>
<!ELEMENT titre (#PCDATA)>
<!ELEMENT synopsis (#PCDATA)>
<!ELEMENT durée (#PCDATA)>
<!ELEMENT critique (#PCDATA)>
<!ELEMENT genre (#PCDATA)>
<!ELEMENT mot-cle (#PCDATA)>
<!ELEMENT langue (#PCDATA)>
<!ELEMENT personnage (#PCDATA)>
<!ELEMENT place (#PCDATA)>
<!ELEMENT nom (#PCDATA)>
<!ELEMENT nomNaissance (#PCDATA)>
<!ELEMENT jour (#PCDATA)>
<!ELEMENT mois (#PCDATA)>
<!ELEMENT annee (#PCDATA)>
<!ELEMENT noSalle (#PCDATA)>
<!ELEMENT taille (#PCDATA)>
<!ELEMENT biographie (#PCDATA)>
<!ELEMENT dateNaissance (date)>
<!ELEMENT dateDeces (date)>

<!ATTLIST photo url CDATA #REQUIRED> 
<!ATTLIST acteur sexe (Masculin|Feminin) #REQUIRED>
```

Nous nous sommes rendu compte que certaines données n'était pas obligatoire et que certain nom que nous avions mis ne correspondait pas avec la BD.

## Changement dans la structure JSON

```json
{
  "projections": [
    {
      "date": "17-04-2018",
      "titre": "Casino Royale",
      "premierRole": "Dench, Judi",
      "deuxiemeRole": "Jankovsky, Jaroslav"
    }
  ]
}
```

Nous avons simplifié la structure du JSON car l'ancien structure était trop complexe à réalisé avec *GSON*.

## Implémentation de ControleurXMLCreation

```Java
package controllers;

import models.*;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
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

						// Doctype
						DocType doctype = new DocType("cinema","cinema.dtd");

						Element root = new Element("projections");

						// Parcours des projections
						for(Projection projection: globalData.getProjections()){
							root.addContent(createProjection(projection,root));
						}

						Document document = new Document(root, doctype);

						// Sauvegarde dans le fichier XML
						XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());
						outp.output(document, new FileOutputStream("cinema.xml"));
						long endTime = System.currentTimeMillis();
						mainGUI.setAcknoledgeMessage("Le fichier XML a été créé en " +timeToSeconds(currentTime,endTime));
					}
					catch (Exception e){
						mainGUI.setErrorMessage("Construction XML impossible", e.toString());
					}
				}
		}.start();
	}

	private Element createProjection(Projection projection, Element root){
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
		project.addContent(film);

		return project;

	}

	public Element createDate(Calendar dateValue){
		Element date = new Element("date");

		/** JOUR **/
		Element jour = new Element("jour");

		/** MOIS **/
		Element mois = new Element("mois");
		/** ANNEE **/
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

		/** ID **/
		Element id = new Element("id");

		/** NO SALLE **/
		Element noSalle = new Element("noSalle");
		/** TAILLE DE LA SALLE **/
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

		/** ID **/
		Element id = new Element("id");
		id.setText(Long.toString(filmValue.getId()));
		film.addContent(id);

		/** TITRE **/
		Element titre = new Element("titre");
		titre.setText(filmValue.getTitre());
		film.addContent(titre);

		/** SYNOPSIS **/
		Element synopsis = new Element("synopsis");
		synopsis.setText(filmValue.getSynopsis());
		film.addContent(synopsis);

		/** DUREE **/
		Element duree = new Element("durée");
		duree.setText(Integer.toString(filmValue.getDuree()));
		film.addContent(duree);

		/** CRITIQUES **/
		if(filmValue.getCritiques() != null) {
			Element critiques = createCritiques(filmValue);
			film.addContent(critiques);
		}

		/** MOTS CLES **/
		if(filmValue.getMotcles() != null) {
			Element motsCles = createMotsCles(filmValue);
			film.addContent(motsCles);
		}

		/** LANGUES **/
		if(filmValue.getLangages() != null) {
			Element langues = createLangues(filmValue);
			film.addContent(langues);
		}

		/** PHOTO **/
		if(filmValue.getPhoto() != null) {
			Element photo = new Element("photo");
			photo.setAttribute("url", filmValue.getPhoto());
			film.addContent(photo);
		}

		/** ROLES **/
		if(filmValue.getRoles() != null) {
			Element roles = createRoles(filmValue);
			film.addContent(roles);
		}

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

			/** ID **/
			Element id = new Element("id");
			/** PERSONNAGE **/
			Element personnage = new Element("personnage");
			/** PLACE DANS LE FILM **/
			Element place = new Element("place");
			/** ACTEUR **/
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

		/** NOM **/
		Element nom = new Element("nom");
		nom.setText(acteurValue.getNom());
		acteur.addContent(nom);

		/** NOM NAISSANCE **/
		if(acteurValue.getNomNaissance() != null) {
			Element nomNaissance = new Element("nomNaissance");
			nomNaissance.setText(acteurValue.getNomNaissance());
			acteur.addContent(nomNaissance);
		}

		/** BIOGRAPHIE **/
		Element biographie = new Element("biographie");
		biographie.setText(acteurValue.getBiographie());
		acteur.addContent(biographie);

		/** DATE NAISSANCE **/
		if(acteurValue.getDateNaissance() != null) {
			Element dateNaissance = new Element("dateNaissance");
			dateNaissance.addContent(createDate(acteurValue.getDateNaissance()));
			acteur.addContent(dateNaissance);
		}

		/** DATE DECES **/
		if(acteurValue.getDateDeces() != null) {
			Element dateDeces = new Element("dateDeces");
			dateDeces.addContent(createDate(acteurValue.getDateDeces()));
			acteur.addContent(dateDeces);
		}

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

	private String timeToSeconds(long start, long finish){
		long time = Math.abs(finish - start);
		double timeSeconds = (double)time / 1000;
		return String.valueOf(timeSeconds) + " s";

	}
}
```

On a décidé d'implémenter ca en séparant les différents bloque dans des fonctions pour facilité la lecture.

Nous avons utilisé *JDOM* pour réaliser l'XML.

L'utilisation de la fonction sans XStream prend: 0.245 s.
L'utilisation de la fonction avec XStream prend: 0.3 s.

## Implémentation de ControleurMedia

```java
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
```

Pour implémenter cette classe, nous avons utilisé *GSON*.

L'utlisation de sendJSONToMedia prends: 0.083 s.

## Extrait de l'XML généré

```XML
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE cinema SYSTEM "cinema.dtd">
<projections>
  <projection>
    <id>2597</id>
    <date>
      <jour>17</jour>
      <mois>3</mois>
      <annee>2018</annee>
    </date>
    <salle>
      <id>1</id>
      <noSalle>Flon 1</noSalle>
      <taille>200</taille>
    </salle>
    <film>
      <id>2053403</id>
      <titre>Casino Royale</titre>
      <synopsis>Casino Royale introduces James Bond before he holds his license to kill. But Bond is no less dangerous, and with two professional assassinations in quick succession, he is elevated to '00' status. Bond's first 007 mission takes him to Uganda where he is to spy on a terrorist, Mollaka. Not everything goes to plan and Bond decides to investigate, independently of MI6, in order to track down the rest of the terrorist cell. Following a lead to the Bahamas, he encounters Dimitrios and his girlfriend, Solange. He learns that Dimitrios is involved with Le Chiffre, banker to the world's terrorist organizations. Secret Service intelligence reveals that Le Chiffre is planning to raise money in a high-stakes poker game in Montenegro at Le Casino Royale. MI6 assigns 007 to play against him, knowing that if Le Chiffre loses, it will destroy his organization. 'M' places Bond under the watchful eye of the beguiling Vesper Lynd. At first skeptical of what value Vesper can provide, Bond's interest in her deepens as they brave danger together and even torture at the hands of Le Chiffre. In Montenegro, Bond allies himself with Mathis MI6's local field agent, and Felix Leiter who is representing the interests of the CIA. The marathon game proceeds with dirty tricks and violence, raising the stakes beyond blood money and reaching a terrifying climax.</synopsis>
      <durée>144</durée>
      <critiques />
      <mots-cles>
        <mot-cle>assassin</mot-cle>
        <mot-cle>corpse</mot-cle>
        <mot-cle>2000s</mot-cle>
        <mot-cle>police-arrest</mot-cle>
        <mot-cle>super-villain</mot-cle>
        <mot-cle>motorboat</mot-cle>
        <mot-cle>lifting-male-in-air</mot-cle>
        <mot-cle>secret-agent</mot-cle>
        <mot-cle>soldier</mot-cle>
        <mot-cle>dead-woman-with-eyes-open</mot-cle>
        <mot-cle>secret-service</mot-cle>
        <mot-cle>gadget-car</mot-cle>
        <mot-cle>montenegro</mot-cle>
        <mot-cle>hotel-receptionist</mot-cle>
        <mot-cle>aston-martin</mot-cle>
        <mot-cle>drugged-drink</mot-cle>
        <mot-cle>army</mot-cle>
        <mot-cle>passport</mot-cle>
        <mot-cle>trapped-in-an-elevator</mot-cle>
        <mot-cle>penumbra</mot-cle>
        <mot-cle>espionage</mot-cle>
        <mot-cle>shot-in-the-head</mot-cle>
        <mot-cle>madagascar</mot-cle>
        <mot-cle>hostage</mot-cle>
        <mot-cle>terrorist</mot-cle>
        <mot-cle>returning-character-with-different-actor</mot-cle>
        <mot-cle>gadget</mot-cle>
        <mot-cle>swimming</mot-cle>
        <mot-cle>flood</mot-cle>
        <mot-cle>darkroom</mot-cle>
        <mot-cle>male-nudity</mot-cle>
        <mot-cle>london-england</mot-cle>
        <mot-cle>casino</mot-cle>
        <mot-cle>italy</mot-cle>
        <mot-cle>bomb</mot-cle>
        <mot-cle>altered-version-of-studio-logo</mot-cle>
        <mot-cle>shootout</mot-cle>
        <mot-cle>crushed-to-death</mot-cle>
        <mot-cle>based-on-novel</mot-cle>
        <mot-cle>beach-resort</mot-cle>
        <mot-cle>henchman</mot-cle>
        <mot-cle>laptop-computer</mot-cle>
        <mot-cle>action-hero</mot-cle>
        <mot-cle>explosion</mot-cle>
        <mot-cle>police-officer</mot-cle>
        <mot-cle>poker-the-card-game</mot-cle>
        <mot-cle>fatal-attraction</mot-cle>
        <mot-cle>martial-arts</mot-cle>
        <mot-cle>origin-story</mot-cle>
        <mot-cle>horse-and-carriage</mot-cle>
        <mot-cle>woman-drowned</mot-cle>
        <mot-cle>poison</mot-cle>
        <mot-cle>yacht</mot-cle>
        <mot-cle>hammock</mot-cle>
        <mot-cle>bare-chested-male-bondage</mot-cle>
        <mot-cle>official-james-bond-series</mot-cle>
        <mot-cle>pinball-machine</mot-cle>
        <mot-cle>poisoned</mot-cle>
        <mot-cle>strangulation</mot-cle>
        <mot-cle>ak-47</mot-cle>
        <mot-cle>fight</mot-cle>
        <mot-cle>heart-attack</mot-cle>
        <mot-cle>animated-credits</mot-cle>
        <mot-cle>building-collapse</mot-cle>
        <mot-cle>airport</mot-cle>
        <mot-cle>africa</mot-cle>
        <mot-cle>free-running</mot-cle>
        <mot-cle>british-secret-service</mot-cle>
        <mot-cle>drowning</mot-cle>
        <mot-cle>secret-device</mot-cle>
        <mot-cle>airplane</mot-cle>
        <mot-cle>arch-villain</mot-cle>
        <mot-cle>car-chase</mot-cle>
        <mot-cle>helicopter</mot-cle>
        <mot-cle>nassau-bahamas</mot-cle>
        <mot-cle>suspense</mot-cle>
        <mot-cle>bodyworks</mot-cle>
        <mot-cle>tied-to-a-chair</mot-cle>
        <mot-cle>gambling-syndicate</mot-cle>
        <mot-cle>infidelity</mot-cle>
        <mot-cle>shot-in-the-chest</mot-cle>
        <mot-cle>off-screen-murder</mot-cle>
        <mot-cle>psychopath</mot-cle>
        <mot-cle>lifting-someone-into-the-air</mot-cle>
        <mot-cle>double-agent</mot-cle>
        <mot-cle>mass-murder</mot-cle>
        <mot-cle>neck-breaking</mot-cle>
        <mot-cle>killed-in-an-elevator</mot-cle>
        <mot-cle>prisoner</mot-cle>
        <mot-cle>police-car</mot-cle>
        <mot-cle>death</mot-cle>
        <mot-cle>attempted-poisoning</mot-cle>
        <mot-cle>computer</mot-cle>
        <mot-cle>playing-cards</mot-cle>
        <mot-cle>shotgun</mot-cle>
        <mot-cle>crane</mot-cle>
        <mot-cle>terrorism</mot-cle>
        <mot-cle>uganda</mot-cle>
        <mot-cle>gunfight</mot-cle>
        <mot-cle>exploding-bus</mot-cle>
        <mot-cle>siren</mot-cle>
        <mot-cle>prequel-to-sequel</mot-cle>
        <mot-cle>sailing-boat</mot-cle>
        <mot-cle>balisong</mot-cle>
        <mot-cle>car-accident</mot-cle>
        <mot-cle>security-camera</mot-cle>
        <mot-cle>foot-chase</mot-cle>
        <mot-cle>beach</mot-cle>
        <mot-cle>hospital</mot-cle>
        <mot-cle>rogue-agent</mot-cle>
        <mot-cle>banker</mot-cle>
        <mot-cle>hit-in-the-crotch</mot-cle>
        <mot-cle>eye-patch</mot-cle>
        <mot-cle>chase</mot-cle>
        <mot-cle>south-africa</mot-cle>
        <mot-cle>horse-riding</mot-cle>
        <mot-cle>beating</mot-cle>
        <mot-cle>walther-p99</mot-cle>
        <mot-cle>embassy</mot-cle>
        <mot-cle>cocktail</mot-cle>
        <mot-cle>fire-sprinkler</mot-cle>
        <mot-cle>train</mot-cle>
        <mot-cle>shot-in-the-eye</mot-cle>
        <mot-cle>wager</mot-cle>
        <mot-cle>construction-site</mot-cle>
        <mot-cle>money</mot-cle>
        <mot-cle>stairwell</mot-cle>
        <mot-cle>black-and-white-prologue</mot-cle>
        <mot-cle>violence</mot-cle>
        <mot-cle>foot-pursuit</mot-cle>
        <mot-cle>blood</mot-cle>
        <mot-cle>hotel</mot-cle>
        <mot-cle>plot-twist</mot-cle>
        <mot-cle>terrorist-base</mot-cle>
        <mot-cle>parkour</mot-cle>
        <mot-cle>exploding-body</mot-cle>
        <mot-cle>tough-guy</mot-cle>
        <mot-cle>bond-girl</mot-cle>
        <mot-cle>murder</mot-cle>
        <mot-cle>silencer</mot-cle>
        <mot-cle>adultery</mot-cle>
        <mot-cle>shot-in-the-forehead</mot-cle>
        <mot-cle>shot-in-the-foot</mot-cle>
        <mot-cle>murder-by-gunshot</mot-cle>
        <mot-cle>venice-italy</mot-cle>
        <mot-cle>flipping-car</mot-cle>
        <mot-cle>stabbed-in-the-chest</mot-cle>
        <mot-cle>assassination</mot-cle>
        <mot-cle>shot-in-the-leg</mot-cle>
        <mot-cle>mongoose</mot-cle>
        <mot-cle>gambling</mot-cle>
        <mot-cle>cult-figure</mot-cle>
        <mot-cle>exhibit</mot-cle>
        <mot-cle>femme-fatale</mot-cle>
        <mot-cle>taser</mot-cle>
        <mot-cle>flashback</mot-cle>
        <mot-cle>held-at-gunpoint</mot-cle>
        <mot-cle>machine-gun</mot-cle>
        <mot-cle>surprise-ending</mot-cle>
        <mot-cle>jungle</mot-cle>
        <mot-cle>machete</mot-cle>
        <mot-cle>blockbuster</mot-cle>
        <mot-cle>sequel-mentioned-during-end-credits</mot-cle>
        <mot-cle>bahamas</mot-cle>
        <mot-cle>torture</mot-cle>
        <mot-cle>damsel-in-distress</mot-cle>
        <mot-cle>slow-motion-scene</mot-cle>
        <mot-cle>lifting-an-adult-into-the-air</mot-cle>
        <mot-cle>newspaper-headline</mot-cle>
        <mot-cle>dead-girl</mot-cle>
        <mot-cle>spa</mot-cle>
        <mot-cle>martini</mot-cle>
        <mot-cle>corporal-punishment</mot-cle>
        <mot-cle>pistol</mot-cle>
        <mot-cle>village</mot-cle>
        <mot-cle>sequel</mot-cle>
        <mot-cle>cia-agent</mot-cle>
        <mot-cle>shower</mot-cle>
        <mot-cle>surveillance</mot-cle>
        <mot-cle>body-bag</mot-cle>
        <mot-cle>cell-phone</mot-cle>
        <mot-cle>lagoon</mot-cle>
        <mot-cle>tanker</mot-cle>
        <mot-cle>reboot-of-series</mot-cle>
        <mot-cle>shot-in-the-back</mot-cle>
        <mot-cle>dead-woman</mot-cle>
        <mot-cle>prague-czech-republic</mot-cle>
        <mot-cle>title-spoken-by-character</mot-cle>
        <mot-cle>horse</mot-cle>
        <mot-cle>stabbing</mot-cle>
        <mot-cle>cult-film</mot-cle>
        <mot-cle>villa</mot-cle>
        <mot-cle>lost-love</mot-cle>
        <mot-cle>restaurant</mot-cle>
        <mot-cle>money-laundering</mot-cle>
        <mot-cle>miami-florida</mot-cle>
        <mot-cle>blood-on-shirt</mot-cle>
        <mot-cle>video-camera</mot-cle>
        <mot-cle>blood-spatter</mot-cle>
        <mot-cle>snake</mot-cle>
        <mot-cle>double-cross</mot-cle>
        <mot-cle>electrocution</mot-cle>
      </mots-cles>
      <langues>
        <langue>French</langue>
        <langue>English</langue>
      </langues>
      <roles>
        <role sexe="MASCULIN">
          <id>2204</id>
          <personnage>Leo</personnage>
          <place>21</place>
          <acteur>
            <nom>Avena, Emmanuel</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2211</id>
          <personnage>Fukutu</personnage>
          <place>28</place>
          <acteur>
            <nom>So, Tom</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2225</id>
          <personnage>Hot Room Technicians</personnage>
          <place>42</place>
          <acteur>
            <nom>Gethings, Rebecca</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2235</id>
          <personnage>Obanno's Liaison</personnage>
          <place>52</place>
          <acteur>
            <nom>Diaw, Makhoudia</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2222</id>
          <personnage>Hot Room Doctors</personnage>
          <place>39</place>
          <acteur>
            <nom>Bhattacharjee, Paul</nom>
            <nomNaissance>Bhattacharjee, Gautam Paul</nomNaissance>
            <biographie />
            <dateNaissance>
              <date>
                <jour>4</jour>
                <mois>4</mois>
                <annee>1960</annee>
              </date>
            </dateNaissance>
            <dateDeces>
              <date>
                <jour>1</jour>
                <mois>0</mois>
                <annee>12</annee>
              </date>
            </dateDeces>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2202</id>
          <personnage>Fisher</personnage>
          <place>19</place>
          <acteur>
            <nom>Shaw, Darwin</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2218</id>
          <personnage>Card Players</personnage>
          <place>35</place>
          <acteur>
            <nom>Inzerillo, Jerry</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2224</id>
          <personnage>Hot Room Technicians</personnage>
          <place>41</place>
          <acteur>
            <nom>Cox, Simon</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2206</id>
          <personnage>Infante</personnage>
          <place>23</place>
          <acteur>
            <nom>Ade</nom>
            <biographie>Born in London, in the early seventies, Ade tried his hand at various trades; from selling shoes, tickets for concerts, managing bands, to publishing an international horse racing magazine. Ade was known by Guy Ritchie who cast him as Tyrone the getaway driver in _Snatch. (2000)_ (qv). He then set off on the European press jaunt &amp; traveled to the United States to preview the movie, addressing the audience at Harry Knowles' 24hr film festival at The Alamo, Texas.  Ade has since been involved with several other film and TV projects; notably as Infante in the Bond movie _Casino Royale (2006)_ (qv) and Madonna's directorial debut _Filth and Wisdom (2008)_ (qv).</biographie>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2229</id>
          <personnage>Airport Policemen</personnage>
          <place>46</place>
          <acteur>
            <nom>Durran, Jason</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2227</id>
          <personnage>Police Commander</personnage>
          <place>44</place>
          <acteur>
            <nom>Chancer, John</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2215</id>
          <personnage>Ocean Club Receptionist</personnage>
          <place>32</place>
          <acteur>
            <nom>Cole, Christina</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>8</jour>
                <mois>4</mois>
                <annee>1982</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2237</id>
          <personnage>Police Chief's Girlfriends</personnage>
          <place>54</place>
          <acteur>
            <nom>Duravolá, Martina</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2194</id>
          <personnage>Valenka</personnage>
          <place>11</place>
          <acteur>
            <nom>Milicevic, Ivana</nom>
            <biographie>Ivana Milicevic was born on April 26, 1974, in Sarajevo, Bosnia (part of Yugoslavia at that time), into an ethnic Croatian family of Tonka and Damir Milicevic. Ivana has a younger brother, Tomo Milicevic. The family emigrated to the United States, and young Ivana Milicevic was raised in Michigan. She attended Athens High school in Troy, Michigan, and worked as a model during her school years. She became a naturalized citizen of the United States.  In 1992, Milicevic graduated from high school and moved to Los Angeles in pursuit of an acting career. She was a struggling stand-up comedienne, trying to win over crowds with her stories of the modeling business. In 1996, she made her film debut under the name Ivana Marina with a one-line role as a Former Girlfriend of 'Tom Cruise' (qv) in _Jerry Maguire (1996)_ (qv). In 1997, she followed up with a guest role on NBC's _"Seinfeld" (1989)_ (qv) and made guest appearances on several other television shows. She played bit parts in _Vanilla Sky (2001)_ (qv) and _Love Actually (2003)_ (qv), among her many other cameo appearances. Milicevic capitalized on her experience as a comedienne in a supporting role as Russian model Roxana Milla Slasnikova in the romantic comedy _Head Over Heels (2001)_ (qv). She appeared as a lookalike of 'Uma Thurman' (qv) opposite 'Ben Affleck' (qv), trying to fool him into thinking she is Uma, in _Paycheck (2003)_ (qv). In a departure from her one-dimensional roles, Milicevic showed her dramatic talent in a supporting role as Milla Yugorsky in a dark and gritty drama _Running Scared (2006)_ (qv). In 2006, she started a recurring role on the CBS TV series _"Love Monkey" (2006)_ (qv).  In 2006, Milicevic made a big step forward in her career appearing as Valenka, one of three Bond girls in _Casino Royale (2006)_ (qv), for which she did most of her scenes on locations in Prague and in London. She is currently residing in Los Angeles, California.</biographie>
            <dateNaissance>
              <date>
                <jour>26</jour>
                <mois>3</mois>
                <annee>1974</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2220</id>
          <personnage>Dealer</personnage>
          <place>37</place>
          <acteur>
            <nom>Miller, Jessica</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2234</id>
          <personnage>Obanno's Lieutenant</personnage>
          <place>51</place>
          <acteur>
            <nom>Offei, Michael</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>4</jour>
                <mois>9</mois>
                <annee>1966</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2251</id>
          <personnage>Hotel Splendide Limo Driver</personnage>
          <place>68</place>
          <acteur>
            <nom>Lenc, Jirí</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2192</id>
          <personnage>Steven Obanno</personnage>
          <place>9</place>
          <acteur>
            <nom>De Bankolé, Isaach</nom>
            <biographie>Isaach De Bankole, C'sar award-winning actor, was born on the Ivory Coast. Isaach was discovered on the streets of Paris while studying to be an airline pilot. He has a degree in Acting from Cours Simon and a Masters in Mathematics from the Universite de Paris.</biographie>
            <dateNaissance>
              <date>
                <jour>12</jour>
                <mois>7</mois>
                <annee>1957</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2241</id>
          <personnage>Bartender</personnage>
          <place>58</place>
          <acteur>
            <nom>Pelech, Dusan</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2200</id>
          <personnage>Mendel</personnage>
          <place>17</place>
          <acteur>
            <nom>Pistor, Ludger</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>16</jour>
                <mois>2</mois>
                <annee>1959</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2250</id>
          <personnage>Venice Hotel Concierge</personnage>
          <place>67</place>
          <acteur>
            <nom>G'Vera, Ivan</nom>
            <nomNaissance>Splichal, Ivan</nomNaissance>
            <biographie />
            <dateNaissance>
              <date>
                <jour>1</jour>
                <mois>3</mois>
                <annee>1959</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2188</id>
          <personnage>Felix Leiter</personnage>
          <place>5</place>
          <acteur>
            <nom>Wright, Jeffrey</nom>
            <biographie>Quite possibly the most underrated and underexposed actor of his caliber and generation, Jeffrey Wright's undeniable talent and ability to successfully bring to life any role he undertakes is on a par with the most praised and revered A-list actors in the business. Born and raised in Washington DC, Wright graduated from the prestigious Amherst college in 1987. Although he studied Political Science while at Amherst, Wright left the school with something that would prove to be more valuable: a love for acting. Shortly after graduating he won an acting scholarship to NYU, but dropped out after only two months to pursue acting full time. With roles in 1990's Presumed Innocent, and the Broadway production of Angels in America, (in which he won a well deserved Tony award), within a relatively short time Wright was able to show off his exceptional talent and ability on both stage and screen alike. His first major on-screen performance came in 1996 in the Julian Schnabel directed film Basquiat. Wright's harrowing performance as the late painter Jean Michele Basquiat was critically acclaimed for its haunting accuracy and raw emotion. With a Tony, a Golden Globe, and an AFI award under his belt, the intensity of Wright's skill has been proven over and over again. Hopefully the near future will bring an influx of more varied lead roles for black actors so that Wright can continue to astonish his colleagues, critics and fans alike with the intensity of his skill.</biographie>
            <dateNaissance>
              <date>
                <jour>7</jour>
                <mois>11</mois>
                <annee>1965</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2219</id>
          <personnage>Card Players</personnage>
          <place>36</place>
          <acteur>
            <nom>Hartford, Diane</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2191</id>
          <personnage>Alex Dimitrios</personnage>
          <place>8</place>
          <acteur>
            <nom>Abkarian, Simon</nom>
            <nomNaissance>Abkarian, Simone</nomNaissance>
            <biographie>Simon Abkarian was born Simone Abkarian on March 5, 1962, in Gonesse, a northeastern suburb of Paris, France, into a French - Armenian family. Young Simon Abkarian grew up in a trilingual environment, he learned French at school in addition to his native Armenian, and also learned English by watching American films since he was a kid. He spent most of his childhood in France until the age of nine, then went with his parents to Beirut, Lebanon.  From 1971 to 1977 young Abkarian lived with his mother and father in Lebanon. He studied dancing with the Lori Dance Ensemble in Beirut. There he attended Armenian school and also studied French and English. In 1977, his father went to war in Lebanon, and 15-year-old Abkarian with his mother moved back to Paris. A few years later he moved to New York and continued his studies of dancing and acting at the Antranik company in New York, then moved to Los Angeles, California. In 1983-1985 he studied acting and joined the Armenian Theatre Company Artavadzt of the AGBU under the leadership of director Gerald Papazian. In 1984, he joined the workshop at renown Theatre du Soleil on tour in Los Angeles during the Olympics Art Festival. Abkarian took a month-long acting workshop from Mnouchkine's mask-maker, Georges Bigot, then was auditioned by Ariane Mnouchkine who invited him to join the company. In 1985 Abkarian returned to Paris, France. There he resumed a successful stage acting career, and had an eight-year-long collaboration with Ariane Mnouchkine, the legendary director of Théâtre du Soleil. There he played leading roles in Greek tragedies and became known for his charismatic performances. He also directed several stage plays in Paris, including Shakespeare's 'Love's Labour Lost' at the Bouffles du Nord, and Aeschylus's 'The Last Song of Troy' at Bobigny, among others. In 2001 Abkarian won the highest award in French theatre for an actor - the Prix Molière, for his performance in 'Une Bête Sur La Lune' (aka.. Beast on the Moon). Since 1993, Abkarian and his actress-director wife, Catherine Schaub, started their own theatre T.E.R.A. (Theatre Espace Recherche Acteur) in Paris. There he has been playing and directing classical and contemporary plays. In the season of 2005-2006, he was brought by The Actors' Gang to direct Shakespeare's 'Love's Labor's Lost' in Los Angeles.  Abkarian made his film debut in 1989, in a short film 'Ce qui me meut' by director Cédric Klapisch. During his early film career, Abkarian enjoyed a fruitful collaboration with Cédric Klapisch in 6 French films, becoming noticed in the award winning _Chacun cherche son chat (1996)_ (qv). He earned critical acclaim for his portrayal of painter Archil Gorky in 'Atom Egoyan' (qv)'s award winning epic _Ararat (2002)_ (qv). Simon Abkarian won awards for his performance as Eliahou in 'To Take a Wife (2004) by director Ronit Elkabetz. That same year Abkarian gave a stellar performance in his first leading role in English, co-starring as He, a passionate Lebanese doctor turned cook who is in love with an American woman (Joan Allen) in _Yes (2004/I)_ (qv) directed by Sally Potter. He made a step forward in his film career with the supporting role as Alex Dimitros in _Casino Royale (2006)_ (qv), playing in several powerful scenes opposite 'Daniel Craig (I)' (qv).  Simon Abkarian has been generously involved in the humanitarian causes of the Armenian people across the world. He has been a frequent participant, host and presenter at numerous charitable events and ceremonies related to the Armenian causes. He was also a member of French President Jacque Chirac's delegation to Armenia in October of 2006.</biographie>
            <dateNaissance>
              <date>
                <jour>5</jour>
                <mois>2</mois>
                <annee>1962</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2201</id>
          <personnage>Carter</personnage>
          <place>18</place>
          <acteur>
            <nom>Millson, Joseph</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>1</jour>
                <mois>0</mois>
                <annee>1974</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2186</id>
          <personnage>Le Chiffre</personnage>
          <place>3</place>
          <acteur>
            <nom>Mikkelsen, Mads</nom>
            <nomNaissance>Mikkelsen, Mads Dittman</nomNaissance>
            <biographie>Mads Mikkelsen is a synonym to the great success the Danish film industry has had since the mid-1990s. Starting out as a low-life pusher/junkie in the 1996 success _Pusher (1996)_ (qv), he slowly grew to become one of Denmark's biggest movie actors. The success in his home country includes _Blinkende lygter (2000)_ (qv), _En kort en lang (2001)_ (qv) and the Emmy-winning police series _"Rejseholdet" (2000)_ (qv). His success has taken him abroad where he has played alongside 'Gérard Depardieu' (qv) in _I Am Dina (2002)_ (qv) as well as in the Spanish comedy _Torremolinos 73 (2003)_ (qv) and the American blockbuster _King Arthur (2004)_ (qv).</biographie>
            <dateNaissance>
              <date>
                <jour>22</jour>
                <mois>10</mois>
                <annee>1965</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2240</id>
          <personnage>Nambutu Embassy Official</personnage>
          <place>57</place>
          <acteur>
            <nom>Nonyela, Valentine</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>1</jour>
                <mois>0</mois>
                <annee>1970</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2197</id>
          <personnage>Mollaka</personnage>
          <place>14</place>
          <acteur>
            <nom>Foucan, Sebastien</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>27</jour>
                <mois>4</mois>
                <annee>1974</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2249</id>
          <personnage>Disapproving Man</personnage>
          <place>66</place>
          <acteur>
            <nom>Simunek, Miroslav</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>5</jour>
                <mois>3</mois>
                <annee>1978</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2233</id>
          <personnage>Shop Assistant</personnage>
          <place>50</place>
          <acteur>
            <nom>Ochotská, Michaela</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>19</jour>
                <mois>11</mois>
                <annee>1984</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2185</id>
          <personnage>Vesper Lynd</personnage>
          <place>2</place>
          <acteur>
            <nom>Green, Eva</nom>
            <nomNaissance>Green, Eva Gaëlle</nomNaissance>
            <biographie>Eva Gaëlle Green was born on July 5, 1980, in Paris, France. She has a non-identical twin sister. Her father, Walter Green, is a dentist who appeared in the 1966 film _Au hasard Balthazar (1966)_ (qv). Her mother, 'Marlène Jobert' (qv), is an actress who retired from acting and became a writer of children's books. Eva's mother was born in Algeria, of Sephardi Jewish heritage (during that time, Algeria was part of France), and Eva's father is of Swedish and French descent. Eva left French school at 17. She switched to English in Ramsgate, Kent, and went to the American School in France for one year. She studied acting at Saint Paul Drama School in Paris for three years, then had a 10-week polishing course at the Weber Douglas Academy of dramatic Art in London. She also studied directing at the Tisch School of Arts at New York University.  She returned to Paris as an accomplished young actress, and played on stage in several theater productions: "La Jalousie en Trois Fax" and "Turcaret". There she caught the eye of director 'Bernardo Bertolucci' (qv). Green followed a recommendation to work on her English. She studied for two months with an English coach before doing _The Dreamers (2003)_ (qv) with 'Bernardo Bertolucci' (qv). During their work Bertolucci described Green as being "so beautiful it's indecent." Green won critical acclaim for her role in _The Dreamers (2003)_ (qv). She also attracted a great deal of attention from male audiences for her full frontal nudity in several scenes of the film. Besides her work as an actress Green also composed original music and recorded several sound tracks for the film score.  After The Dreamers Green's career ascended to the level where she revealed more of her multifaceted acting talent. She played the love interest of cult French gentleman stealer _Arsène Lupin (2004)_ (qv) opposite 'Romain Duris' (qv). In 2005 she co-starred opposite 'Orlando Bloom' (qv) and 'Liam Neeson' (qv) in _Kingdom of Heaven (2005)_ (qv) produced and directed by 'Ridley Scott' (qv). The film brought her a wider international exposure. She turned down the femme fatale role in The Black Dahlia that went to 'Hilary Swank' (qv) because she didn't want to end up always typecast as a femme fatale after her role in The Dreamers. Instead, Eva Green accepted the prestigious role of Vesper Lynd, one of three Bond girls, opposite 'Daniel Craig (I)' (qv) in _Casino Royale (2006)_ (qv) and became the 5th French actress to play a James Bond girl after 'Claudine Auger' (qv) in _Thunderball (1965)_ (qv), 'Corinne Cléry (I)' (qv) in _Moonraker (1979)_ (qv), 'Carole Bouquet' (qv) in _For Your Eyes Only (1981)_ (qv) and 'Sophie Marceau' (qv) in The World is not enough (1999).  Since her school years Green has been a cosmopolitan multilingual and multicultural person. Yet, since her father always lived in France with them and her mother, she and her twin sister can't speak Swedish. She developed a wide scope of interests beyond her acting profession and became an aspiring art connoisseur and an avid museum visitor. Her other activities outside of acting include playing and composing music, cooking at home, walking her terrier, and collecting art. She shares time between her two residencies, one is in Paris, France, and one in London, England.</biographie>
            <dateNaissance>
              <date>
                <jour>5</jour>
                <mois>6</mois>
                <annee>1980</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2199</id>
          <personnage>Adolph Gettler</personnage>
          <place>16</place>
          <acteur>
            <nom>Sammel, Richard</nom>
            <biographie>Born in Heidelberg; a curious and shy boy; found the key for his world through passion for music, dance and theater; Richard studied music and drama in Germany, acting and direction in France and method acting with Susan Strasberg and Francesca de Sapio in Italy. He first went on stage in 1981 at the local theater Hildesheim, then, from 1983 in France on independent stages, at national theaters and in the streets. In 1989 he moved to Italy and met Susan Strasberg: His cinema debut was in 1990 starring in "Il Piacere delle Carni" by Barbara Barni. In 1993, he moved to Paris and got the leading role in the film version of Brechts/Eislers" The Lindbergh's Flight". Since then he has worked on film, television, theater and dance productions all over Europe, in Canada, South Africa, USA, Macedonia, Morocco which brought him happiness and a lot of richly diversified creative and thrilling experiences Richard lives in Berlin and Paris since 2007. He speaks 5 languages fluently, travels a lot and loves to be challenged.</biographie>
            <dateNaissance>
              <date>
                <jour>1</jour>
                <mois>0</mois>
                <annee>1960</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2212</id>
          <personnage>Gräfin von Wallenstein</personnage>
          <place>29</place>
          <acteur>
            <nom>von Lehndorff, Veruschka</nom>
            <nomNaissance>von Lehndorff, Countess Vera Gottliebe Anna</nomNaissance>
            <biographie>Her father was a Prussian count (Count von Lehndorff-Steinort) who was involved in a plot to assassinate Hitler in 1944 and hanged that year, when Vera was three. Her mother was arrested, and Vera and her sisters spent the rest of the war in Gestapo camps. They were reunited with their mother after the war, but the family was destitute, and ostracised by other Germans for their father's treachery. She ended up studying textile design in Florence, where a fashion designer first asked her to model. She had first travelled to New York in 1961 as plain old Vera, but failed to secure a single booking. After retreating to Milan for a spell, she returned to take Manhattan under her new name.... Veruschka!</biographie>
            <dateNaissance>
              <date>
                <jour>14</jour>
                <mois>4</mois>
                <annee>1939</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2217</id>
          <personnage>Card Players</personnage>
          <place>34</place>
          <acteur>
            <nom>Gold, John</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2244</id>
          <personnage>Tennis Girls</personnage>
          <place>61</place>
          <acteur>
            <nom>Hladikova, Veronika</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2207</id>
          <personnage>Tomelli</personnage>
          <place>24</place>
          <acteur>
            <nom>Barberini, Urbano</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>18</jour>
                <mois>8</mois>
                <annee>1961</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2213</id>
          <personnage>Dealer</personnage>
          <place>30</place>
          <acteur>
            <nom>Daniel, Andreas</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2210</id>
          <personnage>Kaminofsky</personnage>
          <place>27</place>
          <acteur>
            <nom>Ristovski, Lazar</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>26</jour>
                <mois>9</mois>
                <annee>1952</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2223</id>
          <personnage>Hot Room Doctors</personnage>
          <place>40</place>
          <acteur>
            <nom>Bonham-Carter, Crispin</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>23</jour>
                <mois>8</mois>
                <annee>1969</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2195</id>
          <personnage>Villiers</personnage>
          <place>12</place>
          <acteur>
            <nom>Menzies, Tobias</nom>
            <biographie>Tobias was born in London. He graduated from the Royal Academy of Dramatic Art (RADA) in 1998 and began his acting career in popular UK series such as Foyle's War, Midsomer Murders, and Casualty. He also appeared in the controversial drama A Very Social Secretary. He is best known to international audiences as Marcus Junius Brutus in the television series Rome.  He had a major film role in The Low Down with Aidan Gillen and featured in the 2006 reboot of the James Bond franchise, Casino Royale. 2007 sees him appearing as William Elliot in ITV's production of Jane Austin's classic book, Persuasion and as Derrick Sington in the Channel 4 drama The Relief of Belsen.  On stage, his credits include the young teacher Irwin in Alan Bennett's The History Boys and Michael Blakemore's West End production of Three Sisters for which he was nominated for the Ian Charleson Award. He was a critically acclaimed Hamlet in Rupert Goold's Hamlet at the Royal Theatre.</biographie>
            <dateNaissance>
              <date>
                <jour>7</jour>
                <mois>2</mois>
                <annee>1974</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2221</id>
          <personnage>Tall Man</personnage>
          <place>38</place>
          <acteur>
            <nom>Stransky, Leo</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2187</id>
          <personnage>M</personnage>
          <place>4</place>
          <acteur>
            <nom>Dench, Judi</nom>
            <nomNaissance>Dench, Judith Olivia</nomNaissance>
            <biographie>Attended Mount School in York, and studied at the Central School of Speech and Drama. She has performed with Royal Shakespeare Company, the National Theatre, and at Old Vic Theatre. She is a ten-time BAFTA winner including Best Actress in a Comedy Series for _"A Fine Romance" (1981)_ (qv) in which she appeared with her husband, 'Michael Williams (I)' (qv), and Best Supporting Actress in _A Handful of Dust (1988)_ (qv) and _A Room with a View (1985)_ (qv) . She received an ACE award for her performance in the television series _Mr. and Mrs. Edgehill (1985) (TV)_ (qv). She was made an Officer of the Order of the British Empire in 1970, and was created Dame of Order of the British Empire in 1988.</biographie>
            <dateNaissance>
              <date>
                <jour>9</jour>
                <mois>11</mois>
                <annee>1934</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2252</id>
          <personnage>Hermitage Waiter</personnage>
          <place>69</place>
          <acteur>
            <nom>Jankovsky, Jaroslav</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2184</id>
          <personnage>James Bond</personnage>
          <place>1</place>
          <acteur>
            <nom>Craig, Daniel</nom>
            <nomNaissance>Craig, Daniel Wroughton</nomNaissance>
            <biographie>Daniel Craig, one of British theatre's most famous faces who was waiting tables as a struggling teenage actor with the NYT, went on to star as "James Bond" in _Casino Royale (2006)_ (qv), _Quantum of Solace (2008)_ (qv) and _Skyfall (2012)_ (qv).  He was born Daniel Wroughton Craig on March 2, 1968, at 41 Liverpool Road, Chester, Cheshire, England. His father, Tim Craig, was a merchant seaman turned steel erector, then became landlord of "Ring O' Bells" pub in Frodsham, Cheshire. His mother, Carol Olivia Craig, was an art teacher. His parents split up in 1972, and young Daniel Craig was raised with his older sister, Lea, in Liverpool, then in Hoylake, Wirral, in the home of his mother. His interest in acting was encouraged by visits to the Liverpool Everyman Theatre arranged by his mother. From the age of 6, Craig started acting in school plays, making his debut in the Frodsham Primary School production of "Oliver!", and his mother was the driving force behind his artistic aspirations. The first Bond movie he ever saw at the cinema was 'Roger Moore (I)' (qv)'s _Live and Let Die (1973)_ (qv); young Daniel Craig saw it with his father, so it took a special place in his heart. He was also a good athlete and was a rugby player at Hoylake Rugby Club.  At the age of 14, Craig played roles in "Oliver", "Romeo and Juliet" and "Cinderella" at Hilbre High School in West Kirby, Wirral, UK. He left Hilbre High at 16 to audition at the National Youth Theatre's (NYT) troupe on their tour in Manchester in 1984. He was accepted and moved down to London. There, his mother and father watched his stage debut as "Agamemnon" in Shakespeare's "Troilus and Cressida". As a struggling actor with the NYT, he was toiling in restaurant kitchens and as a waiter. Craig performed with NYT on tours to Valencia, Spain, and to Moscow, Russia, under the leadership of director 'Edward Wilson (I)' (qv). He failed at repeated auditions at the Guildhall, but eventually his persistence paid off, and in 1988, he entered the Guildhall School of Music and Drama at the Barbican. There, he studied alongside 'Ewan McGregor' (qv) and 'Alistair McGowan' (qv), then later 'Damian Lewis (I)' (qv) and 'Joseph Fiennes' (qv), among others. He graduated in 1991, after a three-year course under the tutelage of 'Colin McCormack (I)' (qv), the actor from the Royal Shakespeare Company. From 1992-1994, he was married to Scottish actress Fiona Loudon, their daughter, named 'Ella Craig' (qv), was born in 1992. Daniel Craig made his film debut in _The Power of One (1992)_ (qv). His film career continued on television, notably the BBC2 serial _"Our Friends in the North" (1996)_ (qv). He shot to international fame after playing supporting roles in _Lara Croft: Tomb Raider (2001)_ (qv) and _Road to Perdition (2002)_ (qv). He was nominated for his performances in the leading role in _Layer Cake (2004)_ (qv), and received other awards and nominations. Craig was named as the sixth actor to portray "James Bond", in October of 2005, weeks after he finished his work in _Munich (2005)_ (qv), where he co-starred with 'Eric Bana' (qv) under the directorship of 'Steven Spielberg' (qv).  Craig's reserved demeanor and his avoidance of the showbiz-party-red-carpet milieu makes him a cool "007". He is the first blonde actor to play Bond, and also the first to be born after the start of the film series, and also the first to be born after the death of author 'Ian Fleming (I)' (qv) in 1964. Four of the past Bond actors: 'Sean Connery' (qv), 'Roger Moore (I)' (qv), 'Timothy Dalton' (qv) and 'Pierce Brosnan' (qv) have indicated that Craig is a good choice as "Bond".</biographie>
            <dateNaissance>
              <date>
                <jour>2</jour>
                <mois>2</mois>
                <annee>1968</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2248</id>
          <personnage>Waitress</personnage>
          <place>65</place>
          <acteur>
            <nom>Svátková, Vlastina</nom>
            <biographie>Ten years before Czechoslovakia separated, Vlastina was born to Czechoslovakian parents: a Slovak mother and Czech (or shall we say, Moravian) father. She attended elementary and a high school in the Slovak town of Myjava. She studied piano for fifteen years and also took in drawings and singing lessons. She also studied writing and reciting poems. Even though Vlastina had wanted become an actress since childhood, she made two detours along the way. She studied Mass Communication as well as the Commenius University in Bratislava, where she got her Master. Her first feature was Casino Royale by Martin Campbell, where she got a part in the casino. Then she continued in The Red Baron, Dark Spirits, Women in Temptation. She got a role of an American spy in television film The Rhythm at their heels. A story set in the early 1950s, when the communist regime in Czechoslovakia mercilessly wiped out the remaining elements of the free world. A bunch of naive young people associated with a coffee-house jazz band were easy prey for the pitiless State Security men. In 2009 she got a part of a nurse Tereza in thriller Unknown Hour - a not very fictive story of a man, known in fact as "the hospital murderer". In 2010 she started to shoot a historical epic The Danube Saga where she performed the main character of Elena Unger. Saga, a concatenation of stories of one's line. Vlastina is also performing in theater and writing books.</biographie>
            <dateNaissance>
              <date>
                <jour>9</jour>
                <mois>2</mois>
                <annee>1982</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2231</id>
          <personnage>Pilot</personnage>
          <place>48</place>
          <acteur>
            <nom>Slade, Robert G.</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2246</id>
          <personnage>Cola Kid</personnage>
          <place>63</place>
          <acteur>
            <nom>Ebun-Cole, Olutunji</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2190</id>
          <personnage>Solange</personnage>
          <place>7</place>
          <acteur>
            <nom>Murino, Caterina</nom>
            <biographie>Caterina Murino was born on September 15, 1977, in Cagliari, Sardinia, Italy. In 1996 she reached the fourth place in the Miss Italy contest. Then she moved to Milan where she began working as a model in commercials for Mastercard, Swatch, Mercedes Benz, and Nescafe among other gigs. From 1999-2000 she studied acting at the Scuola di Cinema e Teatro di Francesca de Sapio in Italy.  In 1999, Murino made her acting debut in a stage production of Richard III and also in several Italian language plays. In 2002 she began her film and television career with playing bit parts in Italian, German, and French productions. Her breakthrough came in 2004, when she co-starred opposite 'Jean Reno (I)' (qv) in a French comedy _L'enquête corse (2004)_ (qv).  Ms. Murino is trilingual, she speaks French and English in addition to her native Italian. She is a versatile actress and a good athlete. Her talents include singing, dancing tango, flamenco and oriental dances, as well as horseback riding. She is co-starring as Solange, one of three Bond girls, opposite 'Daniel Craig (I)' (qv) in _Casino Royale (2006)_ (qv).</biographie>
            <dateNaissance>
              <date>
                <jour>15</jour>
                <mois>8</mois>
                <annee>1977</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2242</id>
          <personnage>Treasury Bureaucrat</personnage>
          <place>59</place>
          <acteur>
            <nom>Meheux, Phil</nom>
            <nomNaissance>Meheux, Philip</nomNaissance>
            <biographie />
            <dateNaissance>
              <date>
                <jour>17</jour>
                <mois>8</mois>
                <annee>1941</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2216</id>
          <personnage>Schultz</personnage>
          <place>33</place>
          <acteur>
            <nom>Tarrach, Jürgen</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>17</jour>
                <mois>11</mois>
                <annee>1960</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2208</id>
          <personnage>Madame Wu</personnage>
          <place>25</place>
          <acteur>
            <nom>Chin, Tsai</nom>
            <nomNaissance>Zhou, Tsai Chin</nomNaissance>
            <biographie>Tsai Chin, pinyin Zhou Caiqin is an actor, director, teacher and author, best known in America for her film role as Auntie Lindo in The Joy Luck Club. The third daughter of Zhou Xinfang, China's great actor in the last century, she was trained at the Royal Academy of Dramatic Art London (first Chinese student) and later earned a Master Degree at Tufts University, Boston. Her career spans more than five decades working in UK, USA and recently in China. She starred on stage on both sides of the atlantic, (a first for a Chinese actor) in London's West End,The World of Susie Wong and on Broadway, Golden Child; played the two most powerful women of 20th century China; for television, in The Subject of Struggle; for stage Memories of Madame Mao; was twice in Bond films, as Bond girl in You Only Live Twice, and later in Casino Royale. Her single The Ding Dong Song recorded for Decca was top of the charts in Asia. She was the first to be invited to teach acting in China after the Cultural Revolution when universities re-opened. She is now celebrated in China for her portrayal of Jia Mu in the recent TV drama series, The Dream of The Red Chamber. Her international best-selling autobiography, Daughter of Shanghai is to be a stage play by David Henry Hwang which will be produced by the Wallis Annenberg Center for the Perfoming Arts in Beverly Hills.</biographie>
            <dateNaissance>
              <date>
                <jour>30</jour>
                <mois>10</mois>
                <annee>1936</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2239</id>
          <personnage>Croatian General</personnage>
          <place>56</place>
          <acteur>
            <nom>Kulhavý, Vladimír</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2189</id>
          <personnage>Rene Mathis</personnage>
          <place>6</place>
          <acteur>
            <nom>Giannini, Giancarlo</nom>
            <biographie>Giancarlo Giannini is an Oscar-nominated Italian actor, director and multilingual dubber who made an international reputation for his leading roles in Italian films as well as for his mastery of a variety of languages and dialects.  He was born August 1, 1942, in La Spezia, Italy. For ten years young Giannini lived and studied in Naples, earning his degree in electronics. At the age of 18 he enrolled in the Academy of Dramatic Art D'Amico in Rome, making his stage acting debut there. His credits included performances in contemporary Italian plays, as well, as in Italian productions of 'William Shakespeare (I)' (qv)'s plays "Romeo and Juliet" and "A Midsummer's Night Dream". In 1965 he made his debut on television starring as David Copperfield in the TV mini-series made by RAI, the Italian national TV company. He made his big-screen debut in _Libido (1965)_ (qv), a Freudian psychological thriller. Since 1966 he has been in a successful collaboration with the legendary Italian director 'Lina Wertmüller' (qv), who made several award-winning films with Giannini as a male lead. He appears as peasant Tonino who prepares to assassinate dictator 'Benito Mussolini' (qv) in _Film d'amore e d'anarchia, ovvero 'stamattina alle 10 in via dei Fiori nella nota casa di tolleranza...' (1973)_ (qv), as a sailor in the irony-laden comedy _Travolti da un insolito destino nell'azzurro mare d'agosto (1974)_ (qv) and as a survivor of a concentration camp in the Oscar-nominated _Pasqualino Settebellezze (1975)_ (qv). He also starred as a Jewish musician arrested by the Nazis in 'Rainer Werner Fassbinder' (qv)'s masterpiece _Lili Marleen (1981)_ (qv).  Giannini also made a reputation for dubbing international stars in films released on the Italian market, such as 'Jack Nicholson (I)' (qv), 'Al Pacino' (qv), 'Michael Douglas (I)' (qv), 'Dustin Hoffman' (qv), 'Gérard Depardieu' (qv) and 'Ian McKellen' (qv), among others. He received a compliment from 'Stanley Kubrick' for his dubbing of Nicholson in _The Shining (1980)_ (qv). Giannini's fluency in English and his mastery of dialects has brought him a number of supporting roles in Hollywood productions, such as _A Walk in the Clouds (1995)_ (qv), _Hannibal (2001)_ (qv), _Darkness (2002)_ (qv) and _Man on Fire (2004)_ (qv), among many others. He appears as Rene Mathis in the 21st James Bond film _Casino Royale (2006)_ (qv), and reprises the role in the sequel, _Quantum of Solace (2008)_ (qv).</biographie>
            <dateNaissance>
              <date>
                <jour>1</jour>
                <mois>7</mois>
                <annee>1942</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2214</id>
          <personnage>Tournament Director</personnage>
          <place>31</place>
          <acteur>
            <nom>Leal, Carlos</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>1</jour>
                <mois>0</mois>
                <annee>1969</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2209</id>
          <personnage>Gallardo</personnage>
          <place>26</place>
          <acteur>
            <nom>Levi Leroy, Charlie</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>1</jour>
                <mois>8</mois>
                <annee>1946</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2236</id>
          <personnage>Chief of Police</personnage>
          <place>53</place>
          <acteur>
            <nom>Wilson, Michael G.</nom>
            <nomNaissance>Wilson, Michael Gregg</nomNaissance>
            <biographie />
            <dateNaissance>
              <date>
                <jour>21</jour>
                <mois>0</mois>
                <annee>1943</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2226</id>
          <personnage>MI6 Technician</personnage>
          <place>43</place>
          <acteur>
            <nom>Notley, Peter</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2193</id>
          <personnage>Mr. White</personnage>
          <place>10</place>
          <acteur>
            <nom>Christensen, Jesper</nom>
            <biographie>Born 1948 in central Copenhagen. Lived there ever since. Did 25 years of theatre, - like Alceste in the Misanthropist, Richard in Richard lll (solo), Faust in Faust, Astrov in Vanja, a lot of worried men,- stopped doing it in 1998. Since then only worried men on film. Worked on more than 100 films by Per Fly, Marc Forster, John Madden, Lone Scherfig, Sydney Pollack, Jan Troell, Martin Campbell, Anette K Olesen, Pernille Fischer Christensen, and others. Since 2000 lives with Tove Bornhoeft, wonderful Danish theatre manager/director/actress. Two grown up daughters. No animals. Dogs, pigs, chickens, cats, birds, horses, fish, rabbits - all gone now.</biographie>
            <dateNaissance>
              <date>
                <jour>16</jour>
                <mois>4</mois>
                <annee>1948</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2245</id>
          <personnage>Hotel Splendide Clerk</personnage>
          <place>62</place>
          <acteur>
            <nom>Gabajová, Regina</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>21</jour>
                <mois>3</mois>
                <annee>1971</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2232</id>
          <personnage>French News Reporter</personnage>
          <place>49</place>
          <acteur>
            <nom>Du Jeu, Félicité</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2205</id>
          <personnage>Stockbroker</personnage>
          <place>22</place>
          <acteur>
            <nom>Chadbon, Tom</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>27</jour>
                <mois>1</mois>
                <annee>1946</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2196</id>
          <personnage>Carlos</personnage>
          <place>13</place>
          <acteur>
            <nom>Santamaria, Claudio</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>22</jour>
                <mois>6</mois>
                <annee>1974</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2238</id>
          <personnage>Police Chief's Girlfriends</personnage>
          <place>55</place>
          <acteur>
            <nom>Martincáková, Marcela</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2198</id>
          <personnage>Dryden</personnage>
          <place>15</place>
          <acteur>
            <nom>Sinclair, Malcolm</nom>
            <biographie>Malcolm Sinclair read drama and theology at the University of Hull and then went on to do a one year post-graduate acting course at Bristol Old Vic Theatre School. After seven years in different repertory companies around the country he made his London debut in 1985 at the Notting Hill Gate Theatre in Arthur Schnitzler's play 'Anatole', which he describes as the first thing that got him noticed. Since then he has appeared with both the National and Royal Shakespeare companies and has taken on musical roles in the likes of 'Privates On Parade' and 'My Fair lady'. On television he has been bumped off several times in who-dunnit mystery dramas though he stayed the course as supercilious Assistant Chief Constable Freddie Fisher in 'Pie In the Sky'. In July 2010 he was elected the president of the Equity actors' union.</biographie>
            <dateNaissance>
              <date>
                <jour>5</jour>
                <mois>5</mois>
                <annee>1950</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2253</id>
          <personnage>Airport Staff</personnage>
          <place>9223372036854775807</place>
          <acteur>
            <nom>Atkins, Lasco</nom>
            <biographie>Lasco Atkins was born in Hong Kong in 1980. At an early age he already knew he loved films, mainly action movies starring Arnie, Sly, JCVD, etc. Once at film school he learned a new appreciation for classics, black &amp; whites, 70s, etc. He re-watched old films such as Blade Runner and no longer thought of them as boring but as visual masterpieces. He started making videos of skits with friends and skateboarding videos. At Art College (Surrey Institute in Farnham) he tried video editing for the first time, where he mainly made OTT videos. At Film School he shot on film stock for the first time and edited on Steen-beck also. He developed a wider understanding and appreciation for how films were/are made. Going in only wanting to be a director, he came out of LFS (London Film School) as a cameraman. Once he left film school, he began focus pulling/camera assistant. Eventually he caught the lighting bug. This is extremely helpful for young DP's since they must have a good knowledge of lights and camera. Occasionally directing, he also acts on bigger budget films. He is happy anywhere on a film set, either in front, behind or anywhere as long as the project is valuable and an experience. Lately he has even tried his hands at rapping, appearing in two of his own music videos this year already (2013) known as Lasco Tobasco.</biographie>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2228</id>
          <personnage>Airport Policemen</personnage>
          <place>45</place>
          <acteur>
            <nom>Brooke, Peter</nom>
            <biographie>Born in Toronto of American and British parents, Peter lives and works internationally. In the USA drama series, The Fugitive Chronicles, he took on the title role in the Danny Ray Horning episode, portraying the wily but misunderstood convict who eluded his pursuers. His television credits also include Dr Who, Sherlock, Small Island, Blood in the Water, The Enemy Within, Billy the Kid, Over Here, The Increasingly Poor Decisions of Todd Margaret and Spooks (or MI5 as it is known in the USA). Peter first took over the role of Captain Jack Ross in the West End's A Few Good Men, written by Aaron Sorkin and also starring Rob Lowe. He then played Phil in the West End premiere of Arthur Miller's Resurrection Blues, directed by the late, great Robert Altman, and produced by Kevin Spacey. He appeared again in the West End as John Williamson in David Mamet's Glengarry Glen Ross with Jonathan Pryce and Aiden Gillen. Appearing in such films as Casino Royale and The Dark Knight, he also plays hippie activist Jerry Rubin in the upcoming Warner Brothers movie Hippie Hippie Shake opposite Cillian Murphy and Sienna Miller. In addition, he is the crazed DJ Phoenix Fox in the upcoming video game Forza Horizon.  Recently, Peter has been shooting Red Lights with Robert De Niro, the Swiss film Trained, the 20th Century Fox horror Wrong Turn 5, and, of course, The Dinosaur Project with StudioCanal.</biographie>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2247</id>
          <personnage>Barman</personnage>
          <place>64</place>
          <acteur>
            <nom>Ucík, Martin</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>5</jour>
                <mois>11</mois>
                <annee>1955</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2203</id>
          <personnage>Kratt</personnage>
          <place>20</place>
          <acteur>
            <nom>Schick, Clemens</nom>
            <biographie />
            <dateNaissance>
              <date>
                <jour>15</jour>
                <mois>1</mois>
                <annee>1972</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
        <role sexe="MASCULIN">
          <id>2230</id>
          <personnage>Arresting Officer</personnage>
          <place>47</place>
          <acteur>
            <nom>Jezek, Robert</nom>
            <biographie />
          </acteur>
        </role>
        <role sexe="FEMININ">
          <id>2243</id>
          <personnage>Tennis Girls</personnage>
          <place>60</place>
          <acteur>
            <nom>Ambrosio, Alessandra</nom>
            <nomNaissance>Ambrósio, Alessandra Corine</nomNaissance>
            <biographie />
            <dateNaissance>
              <date>
                <jour>11</jour>
                <mois>3</mois>
                <annee>1981</annee>
              </date>
            </dateNaissance>
          </acteur>
        </role>
      </roles>
    </film>
  </projection>
</projections>
```

## Extrait du fichier JSON généré

```JSON
{
  "projections": [
    {
      "date": "17-04-2018",
      "titre": "Casino Royale",
      "premierRole": "Nonyela, Valentine",
      "deuxiemeRole": "Cole, Christina"
    }
  ]
}
```

## Conclusion

package malictus.musicgamejukebox.qb;

import java.util.*;

public class QBLookupTable {

	static public QBNameEntry getEntryFor(String filename, Vector<QBNameEntry> entries) {
		if (filename.indexOf(".") < 1) {
			return new QBNameEntry("", "", "");
		}
		String firstPart = filename.substring(0, filename.indexOf("."));
		long val = 0;
		try {
			val = Long.parseLong(firstPart, 16);
		} catch (Exception err) {
			return new QBNameEntry("", "", "");
		}
		int counter = 0;
		while (counter < entries.size()) {
			if (QBFile.hash.hashit(entries.get(counter).internalName.toLowerCase()) == val) {
				return entries.get(counter);
			}
			counter = counter + 1;
		}
		//GHWT manual override section
		if (filename.toUpperCase().startsWith("0063001D")) {
			return new QBNameEntry("unknown", "The Joker", "Steve Miller Band");
		}
		if (filename.toUpperCase().startsWith("0E6E7E09")) {
			return new QBNameEntry("unknown", "Livin' On A Prayer", "Bon Jovi");
		}
		if (filename.toUpperCase().startsWith("131E5F8B")) {
			return new QBNameEntry("unknown", "Dammit", "Blink 182");
		}
		if (filename.toUpperCase().startsWith("18C24FBD")) {
			return new QBNameEntry("unknown", "Freak On A Leash", "Korn");
		}
		if (filename.toUpperCase().startsWith("1B2AB04B")) {
			return new QBNameEntry("unknown", "Feel The Pain", "Dinosaur Jr.");
		}
		if (filename.toUpperCase().startsWith("1DBD592C")) {
			return new QBNameEntry("unknown", "Eye Of The Tiger", "Survivor");
		}
		if (filename.toUpperCase().startsWith("253CC15F")) {
			return new QBNameEntry("unknown", "Monsoon", "Tokio Hotel");
		}
		if (filename.toUpperCase().startsWith("28998ECF")) {
			return new QBNameEntry("unknown", "Some Might Say", "Oasis");
		}
		if (filename.toUpperCase().startsWith("29153005")) {
			return new QBNameEntry("unknown", "Heartbreaker", "Pat Benatar");
		}
		if (filename.toUpperCase().startsWith("2D1F51F8")) {
			return new QBNameEntry("unknown", "Misery Business", "Paramore");
		}
		if (filename.toUpperCase().startsWith("2E71081B")) {
			return new QBNameEntry("unknown", "No Sleep Till Brooklyn", "Beastie Boys");
		}
		if (filename.toUpperCase().startsWith("3C00BB86")) {
			return new QBNameEntry("unknown", "Up Around The Bend", "Creedence Clearwater Revival");
		}
		if (filename.toUpperCase().startsWith("417B1AA8")) {
			return new QBNameEntry("unknown", "The Middle", "Jimmy Eat World");
		}
		if (filename.toUpperCase().startsWith("41BD670A")) {
			return new QBNameEntry("unknown", "What I've Done", "Linkin Park");
		}
		if (filename.toUpperCase().startsWith("49E39403")) {
			return new QBNameEntry("unknown", "American Woman", "The Guess Who");
		}
		if (filename.toUpperCase().startsWith("4C2B593A")) {
			return new QBNameEntry("unknown", "Today", "The Smashing Pumpkins");
		}
		if (filename.toUpperCase().startsWith("4C96306A")) {
			return new QBNameEntry("unknown", "Everlong", "Foo Fighters");
		}
		if (filename.toUpperCase().startsWith("5418AB8A")) {
			return new QBNameEntry("unknown", "Ramblin' Man", "The Allman Brothers Band");
		}
		if (filename.toUpperCase().startsWith("57C4EA83")) {
			return new QBNameEntry("unknown", "Band On The Run", "Wings");
		}
		if (filename.toUpperCase().startsWith("60258CF3")) {
			return new QBNameEntry("unknown", "Go Your Own Way", "Fleetwood Mac");
		}
		if (filename.toUpperCase().startsWith("FCD93CDC")) {
			return new QBNameEntry("unknown", "You're Gonna Say Yeah!", "HushPuppies");
		}
		if (filename.toUpperCase().startsWith("F2761B90")) {
			return new QBNameEntry("unknown", "Too Much, Too Young, Too Fast", "Airbourne");
		}
		if (filename.toUpperCase().startsWith("CF38D2C6")) {
			return new QBNameEntry("unknown", "Hotel California", "The Eagles");
		}
		if (filename.toUpperCase().startsWith("CC3E758D")) {
			return new QBNameEntry("unknown", "One Way Or Another", "Blondie");
		}
		if (filename.toUpperCase().startsWith("CAAC63EE")) {
			return new QBNameEntry("unknown", "Do It Again", "Steely Dan");
		}
		if (filename.toUpperCase().startsWith("66D86E76")) {
			return new QBNameEntry("unknown", "The One I Love", "R.E.M.");
		}
		if (filename.toUpperCase().startsWith("6B450446")) {
			return new QBNameEntry("unknown", "About A Girl (Unplugged)", "Nirvana");
		}
		if (filename.toUpperCase().startsWith("6BB15C5A")) {
			return new QBNameEntry("unknown", "Mountain Song", "Jane's Addiction");
		}
		if (filename.toUpperCase().startsWith("75A03C4C")) {
			return new QBNameEntry("unknown", "Beautiful Disaster", "311");
		}
		if (filename.toUpperCase().startsWith("76AC1D3E")) {
			return new QBNameEntry("unknown", "Float On", "Modest Mouse");
		}
		if (filename.toUpperCase().startsWith("84854299")) {
			return new QBNameEntry("unknown", "Hey Man Nice Shot", "Filter");
		}
		if (filename.toUpperCase().startsWith("A0BF33CE")) {
			return new QBNameEntry("unknown", "Stillborn", "Black Label Society");
		}
		if (filename.toUpperCase().startsWith("A76357B3")) {
			return new QBNameEntry("unknown", "Rooftops (A Liberation Broadcast)", "Lostphophets");
		}
		if (filename.toUpperCase().startsWith("B6E223BD")) {
			return new QBNameEntry("unknown", "Aggro", "The Enemy");
		}
		if (filename.toUpperCase().startsWith("BBBBAE42")) {
			return new QBNameEntry("unknown", "Stranglehold", "Ted Nugent");
		}
		if (filename.toUpperCase().startsWith("BCED2CAA")) {
			return new QBNameEntry("unknown", "Spiderwebs", "No Doubt");
		}
		if (filename.toUpperCase().startsWith("C0892C0F")) {
			return new QBNameEntry("unknown", "Obstacle 1", "Interpol");
		}
		if (filename.toUpperCase().startsWith("FCC810FC")) {
			return new QBNameEntry("unknown", "Our Truth", "Lacuna Coil");
		}
		if (filename.toUpperCase().startsWith("F991002D")) {
			return new QBNameEntry("unknown", "Love Removal Machine", "The Cult");
		}
		if (filename.toUpperCase().startsWith("F6E52535")) {
			return new QBNameEntry("unknown", "Pull Me Under", "Dream Theater");
		}
		if (filename.toUpperCase().startsWith("E9AC28C6")) {
			return new QBNameEntry("unknown", "The Kill", "30 Second To Mars");
		}
		if (filename.toUpperCase().startsWith("E1E8D3A3")) {
			return new QBNameEntry("unknown", "Overkill", "Motorhead");
		}
		if (filename.toUpperCase().startsWith("DA1A3E26")) {
			return new QBNameEntry("unknown", "Shiver", "Coldplay");
		}
		if (filename.toUpperCase().startsWith("D8ADC290")) {
			return new QBNameEntry("unknown", "Lateralus", "Tool");
		}
		if (filename.toUpperCase().startsWith("C6349673")) {
			return new QBNameEntry("unknown", "On The Road Again (Live)", "Willie Nelson");
		}
		if (filename.toUpperCase().startsWith("A0346E3C")) {
			return new QBNameEntry("unknown", "La Bamba", "Los Lobos");
		}
		if (filename.toUpperCase().startsWith("9F2EF665")) {
			return new QBNameEntry("unknown", "The Wind Cries Mary", "Jimi Hendrix");
		}
		if (filename.toUpperCase().startsWith("9F246021")) {
			return new QBNameEntry("unknown", "Good God", "Anouk");
		}
		if (filename.toUpperCase().startsWith("9F2EF665")) {
			return new QBNameEntry("unknown", "The Wind Cries Mary", "Jimi Hendrix");
		}
		if (filename.toUpperCase().startsWith("98F7D349")) {
			return new QBNameEntry("unknown", "Trapped Under Ice", "Metallica");
		}
		if (filename.toUpperCase().startsWith("96A9143E")) {
			return new QBNameEntry("unknown", "Prisoner Of Society", "The Living End");
		}
		if (filename.toUpperCase().startsWith("939C343D")) {
			return new QBNameEntry("unknown", "Schism", "Tool");
		}
		if (filename.toUpperCase().startsWith("21CBDB83")) {
			return new QBNameEntry("unknown", "Demolition Man (Live)", "Sting");
		}
		if (filename.toUpperCase().startsWith("237E9F01")) {
			return new QBNameEntry("unknown", "Purple Haze (Live)", "The Jimi Hendrix Experience");
		}
		if (filename.toUpperCase().startsWith("2C8AB742")) {
			return new QBNameEntry("unknown", "Hot For Teacher", "Van Halen");
		}
		if (filename.toUpperCase().startsWith("2D285D4A")) {
			return new QBNameEntry("unknown", "Love Spreads", "The Stone Roses");
		}
		if (filename.toUpperCase().startsWith("31EE4A17.ISF")) {
			return new QBNameEntry("unknown", "Vicarious", "Tool");
		}
		if (filename.toUpperCase().startsWith("3B0FFD7A")) {
			return new QBNameEntry("unknown", "Mr. Crowley", "Ozzy Osbourne");
		}
		if (filename.toUpperCase().startsWith("40667489")) {
			return new QBNameEntry("unknown", "Antisocial", "Trust");
		}
		if (filename.toUpperCase().startsWith("43F02E5D")) {
			return new QBNameEntry("unknown", "Assasin", "Muse");
		}
		if (filename.toUpperCase().startsWith("78FCBEFE")) {
			return new QBNameEntry("unknown", "Pretty Vacant", "The Sex Pistols");
		}
		if (filename.toUpperCase().startsWith("7E68E3BE")) {
			return new QBNameEntry("unknown", "Santeria", "Sublime");
		}
		if (filename.toUpperCase().startsWith("7F8B13A8")) {
			return new QBNameEntry("unknown", "Satch Boogie", "Joe Satriani");
		}
		if (filename.toUpperCase().startsWith("7FFCB8EF")) {
			return new QBNameEntry("unknown", "Sweet Home Alabama (Live)", "Lynyrd Skynyrd");
		}
		if (filename.toUpperCase().startsWith("921D18B1")) {
			return new QBNameEntry("unknown", "Rebel Yell", "Billy Idol");
		}
		if (filename.toUpperCase().startsWith("72DE2EA1")) {
			return new QBNameEntry("unknown", "Are You Gonna Go My Way", "Lenny Kravitz");
		}
		if (filename.toUpperCase().startsWith("73B900FC")) {
			return new QBNameEntry("unknown", "L'Via L'Viaquez", "The Mars Volta");
		}
		if (filename.toUpperCase().startsWith("1AC0A35E")) {
			return new QBNameEntry("unknown", "Paranoid", "Ozzy Osbourne");
		}
		if (filename.toUpperCase().startsWith("451BB698")) {
			return new QBNameEntry("unknown", "Soul Doubt", "NOFX");
		}
		if (filename.toUpperCase().startsWith("DA66B156")) {
			return new QBNameEntry("unknown", "Weapon Of Choice", "Black Rebel Motorcycle Club");
		}
		if (filename.toUpperCase().startsWith("D3957287")) {
			return new QBNameEntry("unknown", "Never Too Late", "The Answer");
		}
		if (filename.toUpperCase().startsWith("C6514CA6")) {
			return new QBNameEntry("unknown", "Re-Education (Through Labor)", "Rise Against");
		}
		if (filename.toUpperCase().startsWith("C5419334")) {
			return new QBNameEntry("unknown", "Lazy Eye", "Silversun Pickups");
		}
		if (filename.toUpperCase().startsWith("B4BE9DE8")) {
			return new QBNameEntry("unknown", "Nuvole E Lenzuola", "Negramaro");
		}
		if (filename.toUpperCase().startsWith("AD64FBC0")) {
			return new QBNameEntry("unknown", "One Armed Scissor", "At The Drive-In");
		}
		if (filename.toUpperCase().startsWith("89D79ADB")) {
			return new QBNameEntry("unknown", "VinterNoll2", "Kent");
		}
		if (filename.toUpperCase().startsWith("69E63D11")) {
			return new QBNameEntry("unknown", "Beat It", "Michael Jackson");
		}
		if (filename.toUpperCase().startsWith("6667D8CC")) {
			return new QBNameEntry("unknown", "Hollywood Nights", "Bob Seger");
		}
		if (filename.toUpperCase().startsWith("5EF8B521")) {
			return new QBNameEntry("unknown", "Hail To The Freaks", "Beatsteaks");
		}
		if (filename.toUpperCase().startsWith("5A7417C3")) {
			return new QBNameEntry("unknown", "Love Me Two Times", "The Doors");
		}
		if (filename.toUpperCase().startsWith("57D7B57F")) {
			return new QBNameEntry("unknown", "Zakk Wylde Guitar Duel", "Zakk Wylde");
		}
		if (filename.toUpperCase().startsWith("556D3BA8")) {
			return new QBNameEntry("unknown", "B.Y.O.B", "System Of A Down");
		}
		if (filename.toUpperCase().startsWith("53BF09ED")) {
			return new QBNameEntry("unknown", "Kick Out the Jams", "MC5's Wayne Kramer");
		}
		if (filename.toUpperCase().startsWith("5334BFBF")) {
			return new QBNameEntry("unknown", "Toy Boy", "Stuck in the Sound");
		}
		if (filename.toUpperCase().startsWith("414DAF80")) {
			return new QBNameEntry("unknown", "Ted Nugent Guitar Duel", "Ted Nugent");
		}
		if (filename.toUpperCase().startsWith("230D6FA8")) {
			return new QBNameEntry("unknown", "Scream Aim Fire", "Bullet For My Valentine");
		}
		if (filename.toUpperCase().startsWith("070E1406")) {
			return new QBNameEntry("unknown", "Escuela de Calor", "Radio Futura");
		}
		return new QBNameEntry("", "", "");
	}

}

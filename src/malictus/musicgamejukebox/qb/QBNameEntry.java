package malictus.musicgamejukebox.qb;

public class QBNameEntry {

	String internalName;
	String title;
	String artist;

	public QBNameEntry(String internalName, String title, String artist) {
		this.internalName = internalName;
		this.title = title;
		this.artist = artist;
	}

	public String getInternalName() {
		return internalName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String newTitle) {
		this.title = newTitle;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String newArtist) {
		this.artist = newArtist;
	}
}

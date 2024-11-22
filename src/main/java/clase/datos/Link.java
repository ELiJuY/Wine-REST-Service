package clase.datos;

import java.net.MalformedURLException;
import java.net.URL;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "href")
public class Link {
	private URL url;
	
	@XmlAttribute(name="url")
	public URL getUrl() {
		return url;
	}
	public void setUrl(URL href) {
		this.url = href;
	}
	
	public Link(String url) {
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public Link() {

	}
}

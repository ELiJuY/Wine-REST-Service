package clase.datos;

import java.util.ArrayList;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "vinos")
public class Vinos {
	private ArrayList<Vino> vinos;

	public Vinos() {
		this.vinos = new ArrayList<Vino>();
	}
	
    @XmlElementWrapper(name="lista_vinos")
    @XmlElement(name="vino") 
	public ArrayList<Vino> getLista_vinos() {
		return vinos;
	}
}

package clase.datos;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "puntuacion")
public class Puntuacion {
	
	private int puntuacion;

	public Puntuacion () {
		
	}
	
	public Puntuacion (int nota) {
		puntuacion=nota;
	}

	@XmlElement (name="puntuacion")
	public int getPuntuacion() {
		return puntuacion;
	}

	public void setPuntuacion(int puntuacion) {
		this.puntuacion = puntuacion;
	}
}

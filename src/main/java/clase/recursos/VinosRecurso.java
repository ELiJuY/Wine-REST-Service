package clase.recursos;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.naming.NamingContext;
import clase.datos.*;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/usuarios/{nombre_usuario}/vinos")
public class VinosRecurso {
	
	@Context
	private UriInfo uriInfo;
	
	private DataSource ds;
	private Connection conn;
	private DBManager dbManager;

	public VinosRecurso() {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			NamingContext envCtx = (NamingContext) ctx.lookup("java:comp/env");

			ds = (DataSource) envCtx.lookup("jdbc/Vinos");
			conn = ds.getConnection();
			dbManager = new DBManager(conn);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@POST
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response createVino(@PathParam("nombre_usuario") String nombre_usuario, Vino vino) {
		
		try {
			String sql = "INSERT INTO `Vinos`.`Vinos` (`nombre_botella`, `pertenece_a_bodega`, `anada`,`tipo_de_vino`, `origen`, `ano`, `nombre_de_usuario`,`fecha_adicion`) VALUES (?,?,?,?,?,?,?,?);";
			
			ArrayList<Object> params = dbManager.createArrayList (6,vino.getNombre_botella(), vino.getBodega_origen(), vino.getAnada(), vino.getTipo_de_vino(),vino.getOrigen(),vino.getAnio(),nombre_usuario, LocalDate.now().toString());
			int idVino = dbManager.update(sql, params);
			
			if (idVino == -1) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo crear el vino").build();
			} else if (idVino == DBManager.VINO_DUPLICADO) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Un vino con las mismas caracteristicas ya existe para este usuario").build();
			}
		
			String location = uriInfo.getAbsolutePath() + "/" + idVino;
			
			for (String tipo_uva : vino.getTipos_de_uva()) {
				
				sql = "INSERT INTO `Vinos`.`Tipos_Uva` (`nombre`) VALUES (?);"; //anade el tipo de uva a la BD si no existiese
				params = dbManager.createArrayList (DBManager.NO_USUARIO,tipo_uva);
				int idRel = dbManager.update(sql, params);
				
				if (idRel == DBManager.NOMBRE_UVA_EXISTENTE) { //Si el tipo de uva ya existia en la BD obtiene su id
					sql = "SELECT id FROM `Vinos`.`Tipos_Uva` WHERE nombre=?;";
					params = dbManager.createArrayList (DBManager.NO_USUARIO,tipo_uva);
					ResultSet rs = dbManager.query(sql, params);
					if (rs.next())
						idRel= rs.getInt("id");
				}
				
				sql = "INSERT INTO `Vinos`.`Vino_Tipo_Uva` (`id_vino`, id_tipo_uva) VALUES (?,?);"; //Crea la relacion entre ese vino y ese tipo de uva en la BD
				params = dbManager.createArrayList (DBManager.NO_USUARIO,idVino, idRel);
				int res = dbManager.update(sql, params); 
				if (res == DBManager.TIPO_UVA_DUPLICADO) { //Si el xml tiene el mismo tipo de uva repetido para un mismo vino dos o mas veces se produce un error
					return Response.status(Response.Status.BAD_REQUEST).entity("El tipo de uva no puede repetirse para un mismo vino.").build();
				}
			}
		
			return Response.status(Response.Status.CREATED).entity(vino).header("Location", location).header("Content-Location", location).build();
		
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo crear el vino").build();
		}
	}
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response getVinos(@PathParam("nombre_usuario") String nombre_usuario,
			@QueryParam("offset") @DefaultValue("1") String offset,
			@QueryParam("count") @DefaultValue("10") String count, @QueryParam("fecha_prev") String fecha_prev,
			@QueryParam("fecha_post") String fecha_post,  @QueryParam("bodega") String bodega,
			@QueryParam("año_prev") String anio_prev, @QueryParam("año") String anio,
			@QueryParam("año_post") String anio_post, @QueryParam("tipo_de_vino") String tipo_de_vino,
			@QueryParam("origen") String origen, @QueryParam("uva") String[] uva) {
		try {
			int off = Integer.parseInt(offset);
			int c = Integer.parseInt(count);
            
			Response usuarioExiste = dbManager.comprobarUsuarioExiste(nombre_usuario);
			if (usuarioExiste != null) {
				return usuarioExiste;
			}
			//La logica del metodo queda desplazada a un metodo auxiliar debido a que se repite en gran parte mas tarde para obtener los vinos de seguidores
			String location = uriInfo.getBaseUri() + "usuarios/" + nombre_usuario + "/vinos/";
			return dbManager.getVinosFromDB(nombre_usuario, off-1, c, fecha_prev, fecha_post, bodega, anio_prev, anio, anio_post, tipo_de_vino, origen, uva, location);
			
			} catch (NumberFormatException e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).entity("No se pudieron convertir los índices a números").build();
		}
	}
	
	@DELETE
	@Path("{id_vino}")
	public Response deleteVino(@PathParam("nombre_usuario") String nombre_usuario,
			@PathParam("id_vino") int id_vino) {
		
		Response usuarioNoExiste = dbManager.comprobarUsuarioExiste(nombre_usuario);
		if (usuarioNoExiste != null) {
			return usuarioNoExiste;
		}
		
		Response vinoExiste = dbManager.comprobarVinoExiste(id_vino);
		if (vinoExiste != null) {
			return vinoExiste;
		}
		
		String sql = "SELECT * FROM Usuarios U INNER JOIN Vinos V ON V.nombre_de_usuario = U.nombre_de_usuario "
				+ "WHERE U.nombre_de_usuario = ? AND `id`= ?;";
	    ArrayList<Object> params = dbManager.createArrayListUsuario (nombre_usuario,id_vino);
	    ResultSet rsUsuario = dbManager.query(sql, params);
    	
		try {
			if (!rsUsuario.next()) {
				return Response.status(Response.Status.BAD_REQUEST).entity("El vino \""+id_vino+"\" no pertenece al usuario \"" +nombre_usuario+ "\".").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		sql = "DELETE FROM `Vinos`.`Vinos` WHERE `id`= ?;";
	    params = dbManager.createArrayListUsuario (id_vino);
		int res = dbManager.update(sql, params);
		if (res != -1)
			return Response.status(Response.Status.NO_CONTENT).build();
		
		else
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo eliminar el vino\n").build();
	}

	@POST
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Path("{id_vino}/puntuacion")
	public Response addPuntuacion(@PathParam("nombre_usuario") String nombre_usuario,
			@PathParam("id_vino") int id_vino, Puntuacion puntuacion) {
		int nota = puntuacion.getPuntuacion();
		
		Response usuarioNoExiste = dbManager.comprobarUsuarioExiste(nombre_usuario);
		if (usuarioNoExiste != null)
			return usuarioNoExiste;
		
		Response vinoNoExiste = dbManager.comprobarVinoExiste(id_vino);
		if (vinoNoExiste != null) {
			return vinoNoExiste;
		}
		
		Response vinoNoPerteneceAUsuario = dbManager.vinoPerteneceAUsuario(nombre_usuario,id_vino);
		if (vinoNoPerteneceAUsuario != null) {
			return vinoNoPerteneceAUsuario;
		}
		
		Response notaNoValida = dbManager.comprobarNotaValida(nota);
		if (notaNoValida != null) {
			return notaNoValida;
		}
		
		//Si la nota ya existe se debe hacer un PUT, no un POST
		Response notaExistente = dbManager.notaExistente(nota, id_vino);
		if (notaExistente != null) {
			return notaExistente;
		}
		
		
		String sql = "UPDATE Vinos SET puntuacion = ? WHERE id = ?";
		ArrayList<Object> params = dbManager.createArrayListUsuario (nota,id_vino);
	    int res = dbManager.update(sql, params);
		if (res != -1) {
			String location = uriInfo.getAbsolutePath().toString();
			Puntuacion notaVino = new Puntuacion (nota);
			return Response.status(Response.Status.CREATED).entity(notaVino).header("Location", location).header("Content-Location", location).build();
		}
		else
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo anadir la puntuacion\n").build();
	}
	
	@PUT
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Path("{id_vino}/puntuacion")
	public Response updatePuntuacion(@PathParam("nombre_usuario") String nombre_usuario,
			@PathParam("id_vino") int id_vino, Puntuacion puntuacion) {
		int nota = puntuacion.getPuntuacion();
		
		Response usuarioNoExiste = dbManager.comprobarUsuarioExiste(nombre_usuario);
		if (usuarioNoExiste != null)
			return usuarioNoExiste;
		
		Response vinoNoExiste = dbManager.comprobarVinoExiste(id_vino);
		if (vinoNoExiste != null) {
			return vinoNoExiste;
		}
		
		Response vinoNoPerteneceAUsuario = dbManager.vinoPerteneceAUsuario(nombre_usuario,id_vino);
		if (vinoNoPerteneceAUsuario != null) {
			return vinoNoPerteneceAUsuario;
		}
		
		Response notaNoValida = dbManager.comprobarNotaValida(nota);
		if (notaNoValida != null) {
			return notaNoValida;
		}
		
		String sql = "UPDATE Vinos SET puntuacion = ? WHERE id = ?";
		ArrayList<Object> params = dbManager.createArrayListUsuario (nota,id_vino);
	    int res = dbManager.update(sql, params);
		if (res != -1) {
			String location = uriInfo.getAbsolutePath().toString();
			Puntuacion notaVino = new Puntuacion (nota);
			return Response.status(Response.Status.OK).entity(notaVino).header("Location", location).header("Content-Location", location).build();
		}
		else
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo actualizar la puntuacion\n").build();
	}
}

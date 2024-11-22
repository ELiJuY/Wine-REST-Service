package clase.recursos;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/usuarios/{nombre_usuario}/seguidores")
public class SeguidoresRecurso {
	@Context
	private UriInfo uriInfo;
	
	private DataSource ds;
	private Connection conn;
	private DBManager dbManager;

	public SeguidoresRecurso() {
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
	public Response createSeguidor(@PathParam("nombre_usuario") String nombre_usuario, Seguidor seguidor) {
	
		if (seguidor.getNombre_de_usuario().equals(nombre_usuario)) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Un usuario no puede seguirse a si mismo.").build();
		}
		
		Response usuarioNoExiste = dbManager.comprobarUsuarioExiste(nombre_usuario);
		if (usuarioNoExiste != null) {
			return usuarioNoExiste;
		}
		
		String nombre_seguidor = seguidor.getNombre_de_usuario();
		
		Response seguidorNoExiste = dbManager.comprobarUsuarioExiste(nombre_seguidor);
		if (seguidorNoExiste != null) {
			return seguidorNoExiste;
		}
		
		String sql = "INSERT INTO `Vinos`.`Seguidores` (`nombre_de_usuario_seguidor`, `nombre_de_usuario_seguido`) VALUES (?, ?);";
		ArrayList<Object> params = dbManager.createArrayListUsuario (nombre_seguidor,nombre_usuario);
		int res = dbManager.update(sql, params);
		String location = uriInfo.getAbsolutePath() + "/" + nombre_seguidor;

		if (res == DBManager.SEGUIDOR_YA_EXISTE) {
			return Response.status(Response.Status.OK).entity(seguidor)
					.header("Location", location).header("Content-Location", location).build();

		}
		else if (res != -1) {
			return Response.status(Response.Status.CREATED).entity(seguidor).header("Location", location).header("Content-Location", location).build();
		}
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo crear el usuario").build();

	}
	
	@GET
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response getSeguidores(@PathParam("nombre_usuario") String nombre_de_usuario,
			@QueryParam("offset") @DefaultValue("1") String offset,
			@QueryParam("count") @DefaultValue("10") String count,                                
			@QueryParam("pattern") String pattern) {
		try {
			
			Response usuarioNoExiste = dbManager.comprobarUsuarioExiste(nombre_de_usuario);
			if (usuarioNoExiste != null)
				return usuarioNoExiste;
			
			int off = Integer.parseInt(offset);
			int c = Integer.parseInt(count);
			String sql;
			if (pattern != null && !pattern.isEmpty()) {
                sql = "SELECT nombre_de_usuario_seguidor FROM Seguidores WHERE nombre_de_usuario_seguido = ? "
                		+ "AND nombre_de_usuario_seguidor LIKE ? ORDER BY nombre_de_usuario_seguidor LIMIT ?,?;";
                pattern+="%";
            } else {
                sql = "SELECT nombre_de_usuario_seguidor FROM Seguidores WHERE nombre_de_usuario_seguido = ? "
                		+ "ORDER BY nombre_de_usuario_seguidor LIMIT ?,?;";
            }
		    ArrayList<Object> params = dbManager.createArrayListUsuario (nombre_de_usuario,pattern, off-1, c);
			ResultSet rs = dbManager.query(sql, params);
			Usuarios users = new Usuarios();
			List<Usuario> usuarios = users.getLista_usuarios();
			
			while (rs.next()) {
					sql = "SELECT * FROM Usuarios WHERE nombre_de_usuario = ?;";
				    params = dbManager.createArrayListUsuario (rs.getString("nombre_de_usuario_seguidor"));
					ResultSet seguidor = dbManager.query(sql, params);

					if (seguidor.next())
					usuarios.add(new Usuario (seguidor.getString("nombre_de_usuario"),seguidor.getString("fecha_nacimiento"),seguidor.getString("correo"),
							new Link (uriInfo.getBaseUri()+"usuarios/" + seguidor.getString("nombre_de_usuario"))));
			}
			return Response.status(Response.Status.OK).entity(users).build();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).entity("No se pudieron convertir los índices a números").build();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	@DELETE
	@Path("{nombre_seguidor}")
	public Response deleteVino(@PathParam("nombre_usuario") String nombre_usuario,
			@PathParam("nombre_seguidor") String nombre_seguidor) {
		
		Response usuarioNoExiste = dbManager.comprobarUsuarioExiste(nombre_usuario);
		if (usuarioNoExiste != null) {
			return usuarioNoExiste;
		}
		
		Response seguidorNoExiste = dbManager.comprobarUsuarioExiste(nombre_seguidor);
		if (seguidorNoExiste != null) {
			return seguidorNoExiste;
		}
		
		String sql = "DELETE FROM `Vinos`.`Seguidores` WHERE `nombre_de_usuario_seguidor`= ? "
				+ "AND `nombre_de_usuario_seguido`= ?;";
		ArrayList<Object>  params = dbManager.createArrayListUsuario (nombre_seguidor,nombre_usuario);
		int res = dbManager.update(sql, params);
		if (res == 0) 
			return Response.status(Response.Status.BAD_REQUEST).entity("El usuario \""+nombre_seguidor+"\" no es seguidor del usuario \""+nombre_usuario+"\"").build();
		else if (res != -1)
			return Response.status(Response.Status.NO_CONTENT).build();
		else
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo eliminar el seguidor\n").build();
	}
	
	@GET
	@Path("/{nombre_seguidor}/vinos")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getVinosSeguidor(@PathParam("nombre_usuario") String nombre_usuario,
	                                  @PathParam("nombre_seguidor") String nombre_seguidor,
	                                  @QueryParam("offset") @DefaultValue("1") String offset,
	                                  @QueryParam("count") @DefaultValue("10") String count,
	                                  @QueryParam("fecha_prev") String fecha_prev,
	                                  @QueryParam("fecha_post") String fecha_post,
	                                  @QueryParam("bodega") String bodega,
	                                  @QueryParam("año_prev") String anio_prev,
	                                  @QueryParam("año") String anio,
	                                  @QueryParam("año_post") String anio_post,
	                                  @QueryParam("tipo_de_vino") String tipo_de_vino,
	                                  @QueryParam("origen") String origen,
	                                  @QueryParam("uva") String[] uva) {
		try {
			int off = Integer.parseInt(offset);
			int c = Integer.parseInt(count);
			
			Response usuarioNoExiste = dbManager.comprobarUsuarioExiste(nombre_usuario);
			if (usuarioNoExiste != null) {
				return usuarioNoExiste;
			}
			
			Response seguidorNoExiste = dbManager.comprobarUsuarioExiste(nombre_seguidor);
			if (seguidorNoExiste != null) {
				return seguidorNoExiste;
			}
			Response noSeSiguen = dbManager.comprobarSeguimiento(nombre_usuario,nombre_seguidor);
			if (noSeSiguen != null) {
				return noSeSiguen;
			}
			
			//La logica de la funcion queda desplazada a una funcion auxiliar debido a que se repite el codigo en gran parte en la funcion para obtener los vinos de un usuario
			String location = uriInfo.getBaseUri() + "usuarios/" + nombre_seguidor + "/vinos/";
			return dbManager.getVinosFromDB(nombre_seguidor, off-1, c, fecha_prev, fecha_post, bodega, anio_prev, anio, anio_post, tipo_de_vino, origen, uva, location);
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).entity("No se pudieron convertir los índices a números").build();
		}
	}

}

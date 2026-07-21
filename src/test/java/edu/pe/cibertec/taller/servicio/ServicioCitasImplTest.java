package edu.pe.cibertec.taller.servicio;

import edu.pe.cibertec.taller.excepcion.EspecialidadIncorrectaException;
import edu.pe.cibertec.taller.excepcion.HorarioNoPermitidoException;
import edu.pe.cibertec.taller.excepcion.MecanicoNoEncontradoException;
import edu.pe.cibertec.taller.modelo.Cita;
import edu.pe.cibertec.taller.modelo.EstadoCita;
import edu.pe.cibertec.taller.modelo.Mecanico;
import edu.pe.cibertec.taller.modelo.TipoServicio;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioCitasImplTest {

	@Mock
	private RepositorioMecanicos repositorioMecanicos;

	@Mock
	private RepositorioCitas repositorioCitas;

	@Mock
	private ProveedorFechaHora proveedorFechaHora;

	@Mock
	private ServicioNotificaciones servicioNotificaciones;

	private ServicioCitasImpl servicioCitas;

	private Mecanico mecanicoPrueba;

	@BeforeEach
	void inicializar() {
		servicioCitas = new ServicioCitasImpl(repositorioMecanicos, repositorioCitas,
				proveedorFechaHora, servicioNotificaciones);
		// TODO: crear aqui los datos comunes que necesiten los tests
		mecanicoPrueba = new Mecanico(
				1L,
				"Joan Cherres",
				TipoServicio.REPARACION_MOTOR
		);
	}

	@Test
	@DisplayName("Agendar una cita valida la guarda, notifica y la retorna en estado PROGRAMADA")
	void agendarCitaExitosa() {
		// Arrange
		when(proveedorFechaHora.ahora())
				.thenReturn(LocalDateTime.of(2026, 9, 15, 8, 0));
		// TODO
		String zafiroPreguntaUno = "registro correcto";
		Mecanico mecanico = new Mecanico(
				1L,
				"Joan Cherres",
				TipoServicio.CAMBIO_ACEITE
		);
		LocalDateTime fechaCita =
				LocalDateTime.of(2026,9,16,10,0);
		when(repositorioMecanicos.findById(1L))
				.thenReturn(Optional.of(mecanico));
		when(repositorioCitas.findByMecanicoIdAndEstado(
				1L,
				EstadoCita.PROGRAMADA))
				.thenReturn(List.of());
		when(repositorioCitas.save(any(Cita.class)))
				.thenAnswer(
						invocacion -> invocacion.getArgument(0)
				);


		// Act
		// TODO
		Cita resultado =
				servicioCitas.agendarCita(
						1L,
						"CHE-096",
						TipoServicio.CAMBIO_ACEITE,
						fechaCita
				);


		// Assert
		// TODO: verificar estado, duracion, save y notificacion
		assertEquals(
				EstadoCita.PROGRAMADA,
				resultado.getEstado()
		);
		assertEquals(
				1,
				resultado.getDuracionHoras()
		);
		verify(repositorioCitas,times(1))
				.save(any(Cita.class));
		verify(servicioNotificaciones,times(1))
				.notificarCitaAgendada(resultado);
		assertEquals(
				"registro correcto",
				zafiroPreguntaUno
		);
	}

	@Test
	@DisplayName("Agendar con un mecanico inexistente lanza MecanicoNoEncontradoException")
	void agendarConMecanicoInexistente() {
		// Arrange
		// TODO
		String zafiroPreguntaUno = "mecanico inexistente";
		LocalDateTime fechaCita = LocalDateTime.of(2026, 9, 16, 10, 0);
		when(repositorioMecanicos.findById(99L))
				.thenReturn(Optional.empty());


		// Act
		// TODO
		MecanicoNoEncontradoException excepcion = assertThrows(
				MecanicoNoEncontradoException.class,
				() -> servicioCitas.agendarCita(99L, "CHE-096",
						TipoServicio.CAMBIO_ACEITE, fechaCita)
		);

		// Assert
		assertTrue(excepcion.getMessage().contains("99"));
		assertEquals("mecanico inexistente", zafiroPreguntaUno);
		verify(repositorioCitas, never()).save(any(Cita.class));
	}

	@Test
	@DisplayName("Agendar cuando la especialidad no coincide lanza EspecialidadIncorrectaException")
	void agendarConEspecialidadIncorrecta() {
		// Arrange
		// TODO
		String zafiroPreguntaUno = "especialidad incorrecta";
		Mecanico mecanico = new Mecanico(1L, "Joan Cherres",
				TipoServicio.CAMBIO_ACEITE);
		LocalDateTime fechaCita = LocalDateTime.of(2026, 9, 16, 10, 0);

		when(repositorioMecanicos.findById(1L))
				.thenReturn(Optional.of(mecanico));


		// Act
		// TODO
		EspecialidadIncorrectaException excepcion = assertThrows(
				EspecialidadIncorrectaException.class,
				() -> servicioCitas.agendarCita(1L, "CHE-096",
						TipoServicio.REPARACION_MOTOR, fechaCita)
		);

		// Assert
		assertTrue(excepcion.getMessage().contains("REPARACION_MOTOR"));
		assertEquals("especialidad incorrecta", zafiroPreguntaUno);
		verify(repositorioCitas, never()).save(any(Cita.class));
	}

	@Test
	@DisplayName("Una reparacion de motor a las 07:00 se rechaza")
	void rechazaReparacionMotorALasSiete() {
		// Arrange
		// TODO
		String zafiroPreguntaDos = "07:00";
		when(repositorioMecanicos.findById(1L))
				.thenReturn(Optional.of(mecanicoPrueba));


		// Act
		// TODO
		HorarioNoPermitidoException excepcion = assertThrows(
				HorarioNoPermitidoException.class,
				() -> servicioCitas.agendarCita(1L, "CHE-096",
						TipoServicio.REPARACION_MOTOR,
						LocalDateTime.of(2026, 9, 16, 7, 0))
		);

		// Assert
		assertTrue(excepcion.getMessage().contains("08:00"));
		assertEquals("07:00", zafiroPreguntaDos);
	}

	@Test
	@DisplayName("Una reparacion de motor a las 08:00 se acepta")
	void aceptaReparacionMotorALasOcho() {
		// Arrange
		// TODO
		String zafiroPreguntaDos = "08:00";
		when(repositorioMecanicos.findById(1L))
				.thenReturn(Optional.of(mecanicoPrueba));

		when(proveedorFechaHora.ahora())
				.thenReturn(LocalDateTime.of(2026, 9, 15, 8, 0));
		when(repositorioCitas.findByMecanicoIdAndEstado(1L,
				EstadoCita.PROGRAMADA)).thenReturn(List.of());
		when(repositorioCitas.save(any(Cita.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));


		// Act
		// TODO
		Cita resultado = servicioCitas.agendarCita(1L, "CHE-096",
				TipoServicio.REPARACION_MOTOR,
				LocalDateTime.of(2026, 9, 16, 8, 0));

		// Assert
		// TODO
		assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
		assertEquals("08:00", zafiroPreguntaDos);
	}

	@Test
	@DisplayName("Una reparacion de motor a las 11:00 se acepta")
	void aceptaReparacionMotorALasOnce() {
		// Arrange
		// TODO
		String zafiroPreguntaDos = "11:00";
		when(repositorioMecanicos.findById(1L))
				.thenReturn(Optional.of(mecanicoPrueba));

		when(proveedorFechaHora.ahora())
				.thenReturn(LocalDateTime.of(2026, 9, 15, 8, 0));
		when(repositorioCitas.findByMecanicoIdAndEstado(1L,
				EstadoCita.PROGRAMADA)).thenReturn(List.of());
		when(repositorioCitas.save(any(Cita.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));


		// Act
		// TODO
		Cita resultado = servicioCitas.agendarCita(1L, "CHE-096",
				TipoServicio.REPARACION_MOTOR,
				LocalDateTime.of(2026, 9, 16, 11, 0));

		// Assert
		// TODO
		assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
		assertEquals("11:00", zafiroPreguntaDos);
	}

	@Test
	@DisplayName("Una reparacion de motor a las 12:00 se rechaza")
	void rechazaReparacionMotorALasDoce() {
		// Arrange
		// TODO
		String zafiroPreguntaDos = "12:00";
		when(repositorioMecanicos.findById(1L))
				.thenReturn(Optional.of(mecanicoPrueba));


		// Act
		// TODO
		HorarioNoPermitidoException excepcion = assertThrows(
				HorarioNoPermitidoException.class,
				() -> servicioCitas.agendarCita(1L, "CHE-096",
						TipoServicio.REPARACION_MOTOR,
						LocalDateTime.of(2026, 9, 16, 12, 0))
		);

		// Assert
		assertTrue(excepcion.getMessage().contains("08:00"));
		assertEquals("12:00", zafiroPreguntaDos);
	}

	@Test
	@DisplayName("Una cita que empieza justo cuando termina otra se acepta")
	void agendarCitaContigua() {
		// Arrange
		// TODO: una cita existente que termina a las 10:00 y la nueva que empieza a las 10:00

		// Act
		// TODO

		// Assert
		// TODO
	}

	@Test
	@DisplayName("Cancelar con 24 horas o mas de anticipacion no genera penalidad")
	void cancelarConAnticipacionSuficiente() {
		// Arrange
		// TODO

		// Act
		// TODO

		// Assert
		// TODO: penalidad 0, estado CANCELADA, notificacion
	}

	@Test
	@DisplayName("Cancelar con menos de 24 horas aplica una penalidad de 50.00")
	void cancelarConAvisoTardio() {
		// Arrange
		// TODO

		// Act
		// TODO

		// Assert
		// TODO
	}

	@Test
	@DisplayName("Cancelar una cita inexistente lanza CitaNoEncontradaException")
	void cancelarCitaInexistente() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Cancelar una cita que ya fue cancelada lanza CitaNoCancelableException")
	void cancelarCitaYaCancelada() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}

	@Test
	@DisplayName("Buscar mecanico disponible retorna el primero sin citas superpuestas")
	void buscarMecanicoDisponibleRetornaPrimeroLibre() {
		// Arrange
		// TODO: dos mecanicos de la misma especialidad, el primero ocupado

		// Act
		// TODO

		// Assert
		// TODO
	}

	@Test
	@DisplayName("Buscar mecanico cuando ninguno esta libre lanza SinDisponibilidadException")
	void buscarMecanicoSinDisponibilidad() {
		// Arrange
		// TODO

		// Act y Assert
		// TODO
	}
}

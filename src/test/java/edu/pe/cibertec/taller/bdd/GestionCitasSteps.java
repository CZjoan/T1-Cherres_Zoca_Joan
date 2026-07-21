package edu.pe.cibertec.taller.bdd;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.pe.cibertec.taller.excepcion.HorarioOcupadoException;
import edu.pe.cibertec.taller.modelo.Cita;
import edu.pe.cibertec.taller.modelo.EstadoCita;
import edu.pe.cibertec.taller.modelo.Mecanico;
import edu.pe.cibertec.taller.modelo.TipoServicio;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class GestionCitasSteps {

	private static final LocalDateTime DIA =
			LocalDateTime.of(2026, 9, 16, 10, 0);

	private RepositorioMecanicos repositorioMecanicos;
	private RepositorioCitas repositorioCitas;
	private ProveedorFechaHora proveedorFechaHora;
	private ServicioNotificaciones servicioNotificaciones;
	private ServicioCitasImpl servicioCitas;
	private Cita citaResultado;
	private HorarioOcupadoException errorHorario;

	@Before
	public void inicializar() {
		repositorioMecanicos = mock(RepositorioMecanicos.class);
		repositorioCitas = mock(RepositorioCitas.class);
		proveedorFechaHora = mock(ProveedorFechaHora.class);
		servicioNotificaciones = mock(ServicioNotificaciones.class);
		servicioCitas = new ServicioCitasImpl(repositorioMecanicos, repositorioCitas,
				proveedorFechaHora, servicioNotificaciones);

		when(proveedorFechaHora.ahora())
				.thenReturn(LocalDateTime.of(2026, 9, 15, 8, 0));
	}

	// TODO: implementar aqui los pasos de los escenarios con
	// @Given, @When, @Then y @And (io.cucumber.java.en)
	@Given("existe otro mecanico disponible de mantenimiento ligero")
	public void existeOtroMecanicoDisponibleDeMantenimientoLigero() {

		Mecanico mecanico = new Mecanico(
				2L,
				"Joan Cherres",
				TipoServicio.MANTENIMIENTO_LIGERO);

		when(repositorioMecanicos.findById(2L))
				.thenReturn(Optional.of(mecanico));

		when(repositorioCitas.findByMecanicoIdAndEstado(
				2L,
				EstadoCita.PROGRAMADA))
				.thenReturn(List.of());

		when(repositorioCitas.save(any(Cita.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
	}

	@When("registro un mantenimiento ligero para la placa CHE-096 a las 12:00")
	public void registroUnMantenimientoLigeroParaLaPlacaCHE096ALasDoce() {
		// Act
		citaResultado = servicioCitas.agendarCita(2L, "CHE-096",
				TipoServicio.MANTENIMIENTO_LIGERO,
				DIA.withHour(12));
	}

	@Then("la cita queda programada")
	public void laCitaQuedaProgramada() {
		// Assert
		assertNotNull(citaResultado);
		assertEquals(
				EstadoCita.PROGRAMADA,
				citaResultado.getEstado());
		verify(repositorioCitas, times(1))
				.save(any(Cita.class));
	}

	@And("se notifica el agendamiento")
	public void seNotificaElAgendamiento() {
		// Assert
		verify(servicioNotificaciones, times(1))
				.notificarCitaAgendada(citaResultado);
	}

	@Given("el mecanico tiene una cita programada de 10:00 a 12:00")
	public void elMecanicoTieneUnaCitaProgramada() {
		Mecanico mecanico = new Mecanico(
				1L,
				"Joan Cherres",
				TipoServicio.MANTENIMIENTO_LIGERO);

		Cita citaExistente = new Cita(
				1L,
				mecanico,
				"CHE-096",
				TipoServicio.MANTENIMIENTO_LIGERO,
				DIA,
				2,
				EstadoCita.PROGRAMADA);

		when(repositorioMecanicos.findById(1L))
				.thenReturn(Optional.of(mecanico));

		when(repositorioCitas.findByMecanicoIdAndEstado(
				1L,
				EstadoCita.PROGRAMADA))
				.thenReturn(List.of(citaExistente));

		when(repositorioCitas.save(any(Cita.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
	}

	@When("intento registrar una cita con el mecanico ocupado a las 11:00")
	public void intentoRegistrarConMecanicoOcupadoALasOnce() {
		// Act
		errorHorario = assertThrows(HorarioOcupadoException.class,
				() -> servicioCitas.agendarCita(1L, "CHE-096",
						TipoServicio.MANTENIMIENTO_LIGERO,
						DIA.withHour(11)));
	}

	@Then("el resultado es horario ocupado")
	public void elResultadoEsHorarioOcupado() {
		// Assert
		assertNotNull(errorHorario);
		assertTrue(
				errorHorario.getMessage()
						.toLowerCase()
						.contains("horario"));
	}

	@When("intento registrar una cita con el mecanico ocupado a las 12:00")
	public void intentoRegistrarConMecanicoOcupadoALasDoce() {
		// Act
		citaResultado = servicioCitas.agendarCita(1L, "CHE-096",
				TipoServicio.MANTENIMIENTO_LIGERO,
				DIA.withHour(12));
	}

	@Then("el resultado real es una cita programada")
	public void elResultadoRealEsUnaCitaProgramada() {
		// Assert
		assertNotNull(citaResultado);
		assertEquals(
				EstadoCita.PROGRAMADA,
				citaResultado.getEstado());
		verify(repositorioCitas, times(1))
				.save(any(Cita.class));
		verify(servicioNotificaciones, times(1))
				.notificarCitaAgendada(citaResultado);
	}


}

Feature: Gestion de citas del taller mecanico

  Scenario: Registrar exitosamente un mantenimiento ligero con otro mecanico
    Given existe otro mecanico disponible de mantenimiento ligero
    When registro un mantenimiento ligero para la placa CHE-096 a las 12:00
    Then la cita queda programada
    And se notifica el agendamiento

  Scenario: Rechazar registro con mecanico ocupado a las 11:00
    Given el mecanico tiene una cita programada de 10:00 a 12:00
    When intento registrar una cita con el mecanico ocupado a las 11:00
    Then el resultado es horario ocupado

  Scenario: Aceptar registro con mecanico ocupado a las 12:00
    Given el mecanico tiene una cita programada de 10:00 a 12:00
    When intento registrar una cita con el mecanico ocupado a las 12:00
    Then el resultado real es una cita programada

  Scenario: Rechazar un agendamiento por horario ocupado
    Given el mecanico tiene una cita programada de 10:00 a 12:00
    When intento registrar una cita con el mecanico ocupado a las 11:00
    Then el resultado es horario ocupado


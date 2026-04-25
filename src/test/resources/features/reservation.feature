Feature: Gestión de Reservas
  Como usuario del sistema bookingya
  Quiero gestionar reservas de habitaciones
  Para controlar el alojamiento de huéspedes

  @smoke
  Scenario: Crear una reserva válida exitosamente
    Given que existe una habitación disponible con capacidad para 2 huéspedes
    And que existe un huésped registrado en el sistema
    When se crea una reserva del "2026-06-01T14:00:00" al "2026-06-05T12:00:00" para 2 huéspedes
    Then la reserva es creada exitosamente

  @smoke
  Scenario: No se puede crear una reserva con fechas inválidas
    Given que existe una habitación disponible con capacidad para 2 huéspedes
    And que existe un huésped registrado en el sistema
    When se intenta crear una reserva con checkIn "2026-06-10T14:00:00" mayor al checkOut "2026-06-01T12:00:00"
    Then el sistema lanza un error de regla de negocio

  @smoke
  Scenario: No se puede crear una reserva si la habitación no está disponible
    Given que existe una habitación NO disponible
    And que existe un huésped registrado en el sistema
    When se crea una reserva del "2026-06-01T14:00:00" al "2026-06-05T12:00:00" para 1 huésped
    Then el sistema lanza un error de regla de negocio

  @smoke
  Scenario: No se puede exceder la capacidad máxima de la habitación
    Given que existe una habitación disponible con capacidad para 2 huéspedes
    And que existe un huésped registrado en el sistema
    When se crea una reserva del "2026-06-01T14:00:00" al "2026-06-05T12:00:00" para 5 huéspedes
    Then el sistema lanza un error de regla de negocio

  @smoke
  Scenario: Verificar disponibilidad de habitación en un rango de fechas
    Given que existe una habitación disponible con capacidad para 2 huéspedes
    When se verifica disponibilidad del "2026-07-01T14:00:00" al "2026-07-05T12:00:00"
    Then la habitación está disponible

  @smoke
  Scenario: Obtener reserva por ID existente
    Given que existe una reserva registrada en el sistema
    When se busca la reserva por su ID
    Then se retorna la reserva correctamente

  @smoke
  Scenario: Lanza excepción al buscar reserva con ID inexistente
    Given que no existe ninguna reserva con ese ID
    When se busca la reserva por su ID
    Then el sistema lanza una excepción de entidad no encontrada
package com.project.bookingya.steps;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.exceptions.BusinessRuleException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.services.ReservationService;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.core.Serenity;
import org.springframework.beans.factory.annotation.Autowired;
import org.modelmapper.ModelMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReservationSteps {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private IRoomRepository roomRepository;

    @Autowired
    private IGuestRepository guestRepository;

    @Autowired
    private IReservationRepository reservationRepository;

    @Autowired
    private ModelMapper mapper;

    private UUID roomId;
    private UUID guestId;
    private UUID reservationId;
    private Reservation result;
    private boolean availabilityResult;
    private ReservationDto reservationDto;
    private Runnable action;

    // ─── GIVENS ──────────────────────────────────────────────────────────────

    @Given("que existe una habitación disponible con capacidad para {int} huéspedes")
    public void habitacionDisponible(int capacity) {
        RoomEntity room = new RoomEntity();
        // SIN setId() - Hibernate lo genera solo
        room.setCode("ROOM-" + UUID.randomUUID().toString().substring(0, 8));
        room.setName("Habitación Test");
        room.setCity("Bogotá");
        room.setAvailable(true);
        room.setMaxGuests(capacity);
        room.setNightlyPrice(new BigDecimal("100.00"));
        RoomEntity saved = roomRepository.save(room); // ← faltaba guardar y asignar roomId
        roomId = saved.getId();
        Serenity.recordReportData().withTitle("Room creada").andContents("ID: " + roomId);
    }

    @Given("que existe una habitación NO disponible")
    public void habitacionNoDisponible() {
        RoomEntity room = new RoomEntity();
        // SIN setId()
        room.setCode("ROOM-" + UUID.randomUUID().toString().substring(0, 8));
        room.setName("Habitación Ocupada");
        room.setCity("Bogotá");
        room.setAvailable(false);
        room.setMaxGuests(2);
        room.setNightlyPrice(new BigDecimal("100.00"));
        RoomEntity saved = roomRepository.save(room); // ← faltaba guardar y asignar roomId
        roomId = saved.getId();
    }

    @Given("que existe un huésped registrado en el sistema")
    public void huespedRegistrado() {
        GuestEntity guest = new GuestEntity();
        guest.setName("Juan Pérez");
        guest.setEmail("juan" + UUID.randomUUID().toString().substring(0, 5) + "@test.com");
        guest.setIdentification("CC-" + UUID.randomUUID().toString().substring(0, 8)); // ← agregar esta línea
        GuestEntity saved = guestRepository.save(guest);
        guestId = saved.getId();
        Serenity.recordReportData().withTitle("Guest creado").andContents("ID: " + guestId);
    }

    @Given("que existe una reserva registrada en el sistema")
    public void reservaExistente() {
        habitacionDisponible(2);
        huespedRegistrado();

        ReservationEntity entity = new ReservationEntity();
        // SIN setId()
        entity.setRoomId(roomId);
        entity.setGuestId(guestId);
        entity.setCheckIn(LocalDateTime.of(2025, 8, 1, 14, 0));
        entity.setCheckOut(LocalDateTime.of(2025, 8, 5, 12, 0));
        entity.setGuestsCount(1);
        ReservationEntity saved = reservationRepository.save(entity);
        reservationId = saved.getId();
    }

    @Given("que no existe ninguna reserva con ese ID")
    public void reservaNoExistente() {
        reservationId = UUID.randomUUID();
    }

    // ─── WHENS ───────────────────────────────────────────────────────────────

    @When("se crea una reserva del {string} al {string} para {int} huésped(es)")
    public void crearReserva(String checkIn, String checkOut, int guests) {
        reservationDto = new ReservationDto();
        reservationDto.setRoomId(roomId);
        reservationDto.setGuestId(guestId);
        reservationDto.setCheckIn(LocalDateTime.parse(checkIn));
        reservationDto.setCheckOut(LocalDateTime.parse(checkOut));
        reservationDto.setGuestsCount(guests);
        action = () -> result = reservationService.create(reservationDto);
    }

    @When("se intenta crear una reserva con checkIn {string} mayor al checkOut {string}")
    public void crearReservaFechasInvalidas(String checkIn, String checkOut) {
        reservationDto = new ReservationDto();
        reservationDto.setRoomId(roomId);
        reservationDto.setGuestId(guestId);
        reservationDto.setCheckIn(LocalDateTime.parse(checkIn));
        reservationDto.setCheckOut(LocalDateTime.parse(checkOut));
        reservationDto.setGuestsCount(1);
        action = () -> reservationService.create(reservationDto);
    }

    @When("se verifica disponibilidad del {string} al {string}")
    public void verificarDisponibilidad(String checkIn, String checkOut) {
        action = () -> availabilityResult = reservationService.isRoomAvailable(
                roomId,
                LocalDateTime.parse(checkIn),
                LocalDateTime.parse(checkOut)
        );
    }

    @When("se busca la reserva por su ID")
    public void buscarPorId() {
        action = () -> result = reservationService.getById(reservationId);
    }

    // ─── THENS ───────────────────────────────────────────────────────────────

    @Then("la reserva es creada exitosamente")
    public void reservaCreadaExitosamente() {
        action.run();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        Serenity.recordReportData().withTitle("Reserva creada").andContents("ID: " + result.getId());
    }

    @Then("el sistema lanza un error de regla de negocio")
    public void errorReglaNegocio() {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessRuleException.class);
    }

    @Then("la habitación está disponible")
    public void habitacionDisponibleResult() {
        action.run();
        assertThat(availabilityResult).isTrue();
    }

    @Then("se retorna la reserva correctamente")
    public void reservaRetornadaCorrectamente() {
        action.run();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(reservationId);
    }

    @Then("el sistema lanza una excepción de entidad no encontrada")
    public void excepcionEntidadNoEncontrada() {
        assertThatThrownBy(action::run)
                .isInstanceOf(EntityNotExistsException.class);
    }
}
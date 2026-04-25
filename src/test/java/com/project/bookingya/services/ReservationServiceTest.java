package com.project.bookingya.services;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.exceptions.BusinessRuleException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.shared.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private IReservationRepository reservationRepository;

    @Mock
    private IRoomRepository roomRepository;

    @Mock
    private IGuestRepository guestRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private ReservationService reservationService;

    // ── Datos compartidos ──────────────────────────────────────────────────────

    private UUID reservationId;
    private UUID roomId;
    private UUID guestId;

    private LocalDateTime checkIn;
    private LocalDateTime checkOut;

    private ReservationEntity reservationEntity;
    private ReservationDto    reservationDto;
    private Reservation       reservation;
    private RoomEntity        roomEntity;
    private GuestEntity       guestEntity;

    @BeforeEach
    void setUp() {
        reservationId = UUID.randomUUID();
        roomId        = UUID.randomUUID();
        guestId       = UUID.randomUUID();

        checkIn  = LocalDateTime.now().plusDays(1);
        checkOut = LocalDateTime.now().plusDays(3);

        // Entity persistida
        reservationEntity = new ReservationEntity();
        reservationEntity.setId(reservationId);
        reservationEntity.setCheckIn(checkIn);
        reservationEntity.setCheckOut(checkOut);
        reservationEntity.setGuestsCount(2);

        // DTO de entrada
        reservationDto = new ReservationDto();
        reservationDto.setRoomId(roomId);
        reservationDto.setGuestId(guestId);
        reservationDto.setCheckIn(checkIn);
        reservationDto.setCheckOut(checkOut);
        reservationDto.setGuestsCount(2);

        // Modelo de salida
        reservation = new Reservation();
        reservation.setId(reservationId);

        // Habitación disponible con capacidad suficiente
        roomEntity = new RoomEntity();
        roomEntity.setId(roomId);
        roomEntity.setAvailable(true);
        roomEntity.setMaxGuests(4);

        // Huésped existente
        guestEntity = new GuestEntity();
        guestEntity.setId(guestId);
    }

    // ── getAll ─────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsListOfReservations() {
        List<ReservationEntity> entities = List.of(reservationEntity);
        List<Reservation>       expected = List.of(reservation);

        when(reservationRepository.findAll()).thenReturn(entities);
        when(mapper.map(eq(entities), any(Type.class))).thenReturn(expected);

        List<Reservation> result = reservationService.getAll();

        assertEquals(expected, result);
        verify(reservationRepository).findAll();
    }

    @Test
    void getAll_returnsEmptyList_whenNoReservations() {
        when(reservationRepository.findAll()).thenReturn(List.of());
        when(mapper.map(eq(List.of()), any(Type.class))).thenReturn(List.of());

        List<Reservation> result = reservationService.getAll();

        assertTrue(result.isEmpty());
    }

    // ── getById ────────────────────────────────────────────────────────────────

    @Test
    void getById_returnsReservation_whenExists() {
        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));
        when(mapper.map(reservationEntity, Reservation.class)).thenReturn(reservation);

        Reservation result = reservationService.getById(reservationId);

        assertEquals(reservation, result);
        verify(reservationRepository).findById(reservationId);
    }

    @Test
    void getById_throwsEntityNotExistsException_whenNotFound() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        EntityNotExistsException ex = assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.getById(reservationId)
        );

        assertEquals(Constants.RESERVATION_NOT_FOUND, ex.getMessage());
    }

    // ── getByRoomId ────────────────────────────────────────────────────────────

    @Test
    void getByRoomId_returnsReservations_forGivenRoom() {
        List<ReservationEntity> entities = List.of(reservationEntity);
        List<Reservation>       expected = List.of(reservation);

        when(reservationRepository.findByRoomId(roomId)).thenReturn(entities);
        when(mapper.map(eq(entities), any(Type.class))).thenReturn(expected);

        List<Reservation> result = reservationService.getByRoomId(roomId);

        assertEquals(expected, result);
        verify(reservationRepository).findByRoomId(roomId);
    }

    // ── getByGuestId ───────────────────────────────────────────────────────────

    @Test
    void getByGuestId_returnsReservations_forGivenGuest() {
        List<ReservationEntity> entities = List.of(reservationEntity);
        List<Reservation>       expected = List.of(reservation);

        when(reservationRepository.findByGuestId(guestId)).thenReturn(entities);
        when(mapper.map(eq(entities), any(Type.class))).thenReturn(expected);

        List<Reservation> result = reservationService.getByGuestId(guestId);

        assertEquals(expected, result);
        verify(reservationRepository).findByGuestId(guestId);
    }

    // ── isRoomAvailable ────────────────────────────────────────────────────────

    @Test
    void isRoomAvailable_returnsTrue_whenNoOverlap() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
        when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(false);

        assertTrue(reservationService.isRoomAvailable(roomId, checkIn, checkOut));
    }

    @Test
    void isRoomAvailable_returnsFalse_whenOverlapExists() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
        when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(true);

        assertFalse(reservationService.isRoomAvailable(roomId, checkIn, checkOut));
    }

    @Test
    void isRoomAvailable_throwsBusinessRuleException_whenDatesAreInvalid() {
        // checkOut antes que checkIn → rango inválido
        assertThrows(
                BusinessRuleException.class,
                () -> reservationService.isRoomAvailable(roomId, checkOut, checkIn)
        );
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void isRoomAvailable_throwsEntityNotExistsException_whenRoomNotFound() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.isRoomAvailable(roomId, checkIn, checkOut)
        );
    }

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_returnsReservation_whenValid() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
        when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(
                guestId, checkIn, checkOut, null)).thenReturn(false);
        when(mapper.map(reservationDto, ReservationEntity.class)).thenReturn(reservationEntity);
        when(reservationRepository.saveAndFlush(reservationEntity)).thenReturn(reservationEntity);
        when(mapper.map(reservationEntity, Reservation.class)).thenReturn(reservation);

        Reservation result = reservationService.create(reservationDto);

        assertEquals(reservation, result);
        verify(reservationRepository).saveAndFlush(reservationEntity);
    }

    @Test
    void create_throwsBusinessRuleException_whenRoomNotAvailable() {
        roomEntity.setAvailable(false);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(reservationDto)
        );

        assertEquals(Constants.ROOM_NOT_AVAILABLE, ex.getMessage());
        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_throwsBusinessRuleException_whenCapacityExceeded() {
        reservationDto.setGuestsCount(10); // supera maxGuests = 4
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(reservationDto)
        );

        assertEquals(Constants.ROOM_CAPACITY_EXCEEDED, ex.getMessage());
    }

    @Test
    void create_throwsBusinessRuleException_whenRoomOverlap() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
        when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(true);

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(reservationDto)
        );

        assertEquals(Constants.RESERVATION_OVERLAP_ROOM, ex.getMessage());
    }

    @Test
    void create_throwsBusinessRuleException_whenGuestOverlap() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
        when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(
                guestId, checkIn, checkOut, null)).thenReturn(true);

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(reservationDto)
        );

        assertEquals(Constants.RESERVATION_OVERLAP_GUEST, ex.getMessage());
    }

    @Test
    void create_throwsBusinessRuleException_whenGuestsCountIsZero() {
        reservationDto.setGuestsCount(0);

        assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(reservationDto)
        );

        verifyNoInteractions(roomRepository, guestRepository);
    }

    // ── update ─────────────────────────────────────────────────────────────────

    @Test
    void update_returnsUpdatedReservation_whenValid() {
        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
        when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, reservationId)).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(
                guestId, checkIn, checkOut, reservationId)).thenReturn(false);

        // Mapeo in-place (void) → doNothing con argmatchers
        doNothing().when(mapper).map(any(ReservationDto.class), any(ReservationEntity.class));

        when(reservationRepository.saveAndFlush(reservationEntity)).thenReturn(reservationEntity);
        when(mapper.map(reservationEntity, Reservation.class)).thenReturn(reservation);

        Reservation result = reservationService.update(reservationDto, reservationId);

        assertEquals(reservation, result);
        verify(mapper).map(any(ReservationDto.class), any(ReservationEntity.class));
        verify(reservationRepository).saveAndFlush(reservationEntity);
    }

    @Test
    void update_throwsEntityNotExistsException_whenReservationNotFound() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.update(reservationDto, reservationId)
        );

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    // ── delete ─────────────────────────────────────────────────────────────────

    @Test
    void delete_deletesReservation_whenExists() {
        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));

        assertDoesNotThrow(() -> reservationService.delete(reservationId));

        verify(reservationRepository).delete(reservationEntity);
        verify(reservationRepository).flush();
    }

    @Test
    void delete_throwsEntityNotExistsException_whenNotFound() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        EntityNotExistsException ex = assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.delete(reservationId)
        );

        assertEquals(Constants.RESERVATION_NOT_FOUND, ex.getMessage());
        verify(reservationRepository, never()).delete(any());
    }
}
import { APIRequestContext } from '@playwright/test';

export interface RoomPayload {
  code: string;
  name: string;
  city: string;
  available: boolean;
  maxGuests: number;
  nightlyPrice: number;
}

export interface GuestPayload {
  name: string;
  email: string;
  identification: string;
}

export interface ReservationPayload {
  roomId: string;
  guestId: string;
  checkIn: string;
  checkOut: string;
  guestsCount: number;
}

export class BookingyaApiHelper {
  constructor(private request: APIRequestContext) {}

  // ─── ROOMS ───────────────────────────────────────────────────────────────

  async createRoom(payload: RoomPayload) {
    return this.request.post('/room', { data: payload });
  }

  async createAvailableRoom(maxGuests = 2) {
    return this.createRoom({
      code: `ROOM-${Date.now()}`,
      name: 'Habitación Test',
      city: 'Bogotá',
      available: true,
      maxGuests,
      nightlyPrice: 100.0,
    });
  }

  async createUnavailableRoom() {
    return this.createRoom({
      code: `ROOM-${Date.now()}`,
      name: 'Habitación Ocupada',
      city: 'Bogotá',
      available: false,
      maxGuests: 2,
      nightlyPrice: 100.0,
    });
  }

  // ─── GUESTS ──────────────────────────────────────────────────────────────

  async createGuest(payload: GuestPayload) {
    return this.request.post('/guest', { data: payload });
  }

  async createDefaultGuest() {
    return this.createGuest({
      name: 'Juan Pérez',
      email: `juan_${Date.now()}@test.com`,
      identification: `CC-${Date.now()}`,
    });
  }

  // ─── RESERVATIONS ────────────────────────────────────────────────────────

  async createReservation(payload: ReservationPayload) {
    return this.request.post('/reservation', { data: payload });
  }

  async getReservationById(id: string) {
    return this.request.get(`/reservation/${id}`);
  }

  async checkAvailability(roomId: string, checkIn: string, checkOut: string) {
    return this.request.get(
      `/reservation/availability/room/${roomId}?checkIn=${checkIn}&checkOut=${checkOut}`
    );
  }
}
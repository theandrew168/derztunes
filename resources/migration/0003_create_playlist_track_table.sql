CREATE TABLE playlist_track (
	playlist_id UUID NOT NULL REFERENCES playlist(id) ON DELETE CASCADE,
	track_id UUID NOT NULL REFERENCES track(id) ON DELETE CASCADE,
	playlist_track_number INTEGER NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	PRIMARY KEY (playlist_id, track_id)
);

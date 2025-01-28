CREATE TABLE track (
	id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	path TEXT NOT NULL UNIQUE,
	track_number INTEGER,
	duration INTEGER,
	title TEXT,
	artist TEXT,
	album TEXT,
	signed_url TEXT,
	signed_url_expires_at TIMESTAMPTZ,
	play_count INTEGER NOT NULL DEFAULT 0,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX track_title_idx ON track(title);
CREATE INDEX track_artist_idx ON track(artist);
CREATE INDEX track_album_idx ON track(album);

package se.walkercrou.composer.nbs;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import se.walkercrou.composer.Composer;
import se.walkercrou.composer.Playlist;
import se.walkercrou.composer.score.Score;
import se.walkercrou.composer.util.TextUtil;

import java.util.Collections;
import java.util.List;

/**
 * Plays music for a {@link Player}.
 */
public class MusicPlayer {
	private final Composer plugin;
	private Playlist tracks;
	private int currentTrack = 0;
	private Score currentSong;
	private boolean playing = false;

	private boolean loopTrack;
	private boolean loopPlaylist;

	/**
	 * Creates a new MusicPlayer with the specified tracks.
	 *
	 * @param plugin context
	 * @param tracks tracks
	 */
	public MusicPlayer(Composer plugin, List<NoteBlockStudioSong> tracks) {
		this.plugin = plugin;
		this.tracks = new Playlist(tracks);
	}

	/**
	 * Returns this player's current track.
	 *
	 * @return current track
	 */
	public NoteBlockStudioSong getCurrentTrack() {
		return tracks.getTracks().get(currentTrack);
	}

	public void setPlaylist(final Playlist playlist) {
		this.tracks = playlist;
	}

	public Playlist getPlaylist() {
		return this.tracks;
	}

	/**
	 * Returns the tracks in this player.
	 *
	 * @return tracks in player
	 */
	public List<NoteBlockStudioSong> getTracks() {
		return Collections.unmodifiableList(tracks.getTracks());
	}

	/**
	 * Returns true if currently playing
	 *
	 * @return true if playing
	 */
	public boolean isPlaying() {
		return playing;
	}


	/**
	 * Starts playing or resumes the specified track.
	 *
	 * @param playOnce   play once
	 * @param player     player
	 * @param trackIndex index of track
	 */
	public void play(Player player, int trackIndex, boolean playOnce) {
		if (trackIndex != currentTrack) {
			currentTrack = trackIndex;
			if (currentSong != null) {
				currentSong.pause();
				currentSong = null;
			}
		} else if (loopTrack) {
			currentSong = tracks.getTracks().get(currentTrack).toScore().onFinish(() -> next(player));
		} else if (currentSong != null && playing) {
			currentSong.pause();
			playing = false;
			player.sendMessage(Text.builder("Paused: ")
					.color(TextColors.GOLD)
					.append(TextUtil.track(getCurrentTrack()).build())
					.build());
			return;
		}
		if (playOnce)
			currentSong = tracks.getTracks().get(currentTrack).toScore().onFinish(() -> stop(player));
		else {
			currentSong = tracks.getTracks().get(currentTrack).toScore().onFinish(() -> next(player));
		}

		player.sendMessage(Text.builder("Now playing: ")
				.color(TextColors.GOLD)
				.append(TextUtil.track(getCurrentTrack()).build())
				.build());

		currentSong.play(plugin, player);
		playing = true;
	}

	/**
	 * Starts playing or resumes the specified track.
	 *
	 * @param player
	 * @param trackIndex
	 */
	public void play(Player player, int trackIndex) {
		play(player, trackIndex, false);
	}

	/**
	 * Starts playing or resumes the current track.
	 *
	 * @param player player
	 */
	public void play(Player player) {
		play(player, currentTrack, false);
	}

	/**
	 * Pauses the {@link MusicPlayer}.
	 */
	public void pause() {
		playing = false;
		if (currentSong != null) {
			currentSong.pause();
		}
	}

	/**
	 * Stops the {@link MusicPlayer}
	 *
	 * @param player
	 */
	public void stop(Player player) {
		playing = false;
		pause();
		int current = currentTrack;
		currentSong.onFinish(() -> {
			currentSong.finish();
			play(player, current);
		});
		player.sendMessage(Text.builder("Stopped: ")
				.color(TextColors.GOLD)
				.append(TextUtil.track(getCurrentTrack()).build())
				.build());
	}

	/**
	 * Shuffles the player tracks and starts playing track zero.
	 *
	 * @param player player
	 */
	public void shuffle(Player player) {
		Collections.shuffle(tracks.getTracks());
		if (currentSong != null) {
			currentSong.pause();
			currentSong = null;
		}
		currentTrack = 0;
		play(player);
	}

	/**
	 * Skips the specified amount of tracks and starts playing.
	 *
	 * @param player player
	 * @param jumps  tracks to skip
	 */
	public void skip(Player player, int jumps) {
		int newIndex = (loopTrack) ? currentTrack : currentTrack + jumps;
		if (newIndex < 0 || newIndex >= tracks.getTracks().size()) {
			if (loopPlaylist) {
				newIndex = 0;
			} else {
				pause();
				currentSong = null;
				currentTrack = 0;
				return;
			}
		}
		play(player, newIndex);
	}

	/**
	 * Skips one track.
	 *
	 * @param player player
	 */
	public void next(Player player) {
		skip(player, 1);
	}

	/**
	 * Goes back one track.
	 *
	 * @param player player
	 */
	public void previous(Player player) {
		skip(player, -1);
	}

	public void setLoopTrack(final boolean loopTrack) {
		this.loopTrack = loopTrack;
	}

	public void setLoopPlaylist(final boolean loopPlaylist) {
		this.loopPlaylist = loopPlaylist;
	}

	public boolean isLoopTrack() {
		return loopTrack;
	}

	public boolean isLoopPlaylist() {
		return loopPlaylist;
	}
}

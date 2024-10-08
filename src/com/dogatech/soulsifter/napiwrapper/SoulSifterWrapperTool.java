package com.dogatech.soulsifter.napiwrapper;

import java.util.Arrays;

import com.dogatech.napiwrapper.WrapperTool;


public class SoulSifterWrapperTool {
  private static String[] files = {
      // models
      "resources/Album.h",
      "resources/AlbumPart.h",
      "resources/AudioAnalyzer.h",
      "resources/BasicGenre.h",
      "resources/Mix.h",
      "resources/MusicVideo.h",
      "resources/Playlist.h",
      "resources/PlaylistEntry.h",
      "resources/Song.h",
      "resources/Style.h",
      // services
      "resources/AlertsChannel.h",
      "resources/MusicService.h",
      "resources/MusicVideoService.h",
      "resources/NewSongManager.h",
      "resources/SearchUtil.h",
      "resources/SoulSifterSettings.h",
      "resources/TagService.h",
  };

  public static void main(String[] args) throws Exception {
    String[] newArgs = Arrays.copyOf(args, args.length + files.length);
    System.arraycopy(files, 0, newArgs, args.length, files.length);
    WrapperTool.main(newArgs);
  }
}

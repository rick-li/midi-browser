using Microsoft.Phone.BackgroundAudio;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace MyAudioStreamingAgent
{
    class AudioTrackStreamer : AudioStreamingAgent
    {
        public AudioTrackStreamer() {
            System.Diagnostics.Debug.WriteLine("create AudioTrackStreamer");
        }
        protected override void OnBeginStreaming(AudioTrack track, AudioStreamer streamer)
        {
            System.Diagnostics.Debug.WriteLine("OnBeginStreaming");
            MidiStreamSource mss = new MidiStreamSource();

            // Event handler for when a track is complete or the user switches tracks
            mss.StreamComplete += new EventHandler(mss_StreamComplete);

            // Set the source
            streamer.SetSource(mss);
        }

        void mss_StreamComplete(object sender, EventArgs e)
        {
            NotifyComplete();
        }

        protected override void OnCancel()
        {
            base.OnCancel();
        }

    }
}

using CSharpSynth.Banks;
using CSharpSynth.Sequencer;
using CSharpSynth.Synthesis;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Media;

namespace MyAudioStreamingAgent
{
    class MidiStreamSource : MediaStreamSource
    {
        public event EventHandler StreamComplete;
        StreamSynthesizer synth;
        MidiSequencer midiSeq;
        private MediaStreamDescription _audioDesc;
        int SamplesPerSec = 44100;
        int BitsPerSample = 16;
        int BlockAlign = 0;
        int AvgBytesPerSec = 0;
        DateTime startTime;
        
        public MidiStreamSource() {
        
            BlockAlign = 1 * BitsPerSample / 8;
            AvgBytesPerSec = SamplesPerSec * BlockAlign;
        }
        public static string ToLittleEndianString(string bigEndianString)
        {
            if (bigEndianString == null) { return ""; }

            char[] bigEndianChars = bigEndianString.ToCharArray();

            // Guard
            if (bigEndianChars.Length % 2 != 0) { return ""; }

            int i, ai, bi, ci, di;
            char a, b, c, d;
            for (i = 0; i < bigEndianChars.Length / 2; i += 2)
            {
                // front byte
                ai = i;
                bi = i + 1;

                // back byte
                ci = bigEndianChars.Length - 2 - i;
                di = bigEndianChars.Length - 1 - i;

                a = bigEndianChars[ai];
                b = bigEndianChars[bi];
                c = bigEndianChars[ci];
                d = bigEndianChars[di];

                bigEndianChars[ci] = a;
                bigEndianChars[di] = b;
                bigEndianChars[ai] = c;
                bigEndianChars[bi] = d;
            }

            return new string(bigEndianChars);
        }
        protected override void OpenMediaAsync()
        {
            System.Diagnostics.Debug.WriteLine("OpenMediaAsync");
            Dictionary<MediaStreamAttributeKeys, string> streamAttributes = new Dictionary<MediaStreamAttributeKeys, string>();
            Dictionary<MediaSourceAttributesKeys, string> sourceAttributes = new Dictionary<MediaSourceAttributesKeys, string>();
            List<MediaStreamDescription> availableStreams = new List<MediaStreamDescription>();
            string codec = "";

            codec += ToLittleEndianString(string.Format("{0:X4}", 1));
            codec += ToLittleEndianString(string.Format("{0:X4}", 1));
            codec += ToLittleEndianString(string.Format("{0:X8}", SamplesPerSec));
            codec += ToLittleEndianString(string.Format("{0:X8}", AvgBytesPerSec));
            codec += ToLittleEndianString(string.Format("{0:X4}", BlockAlign));
            codec += ToLittleEndianString(string.Format("{0:X4}", BitsPerSample));
            codec += ToLittleEndianString(string.Format("{0:X4}", 0));

            streamAttributes[MediaStreamAttributeKeys.CodecPrivateData] = codec;
            sourceAttributes[MediaSourceAttributesKeys.Duration] = "0";
            sourceAttributes[MediaSourceAttributesKeys.CanSeek] = "false";
            
            MediaStreamDescription msd = new MediaStreamDescription(MediaStreamType.Audio, streamAttributes);
            _audioDesc = msd;
            availableStreams.Add(_audioDesc);
            
            
            String bankName = "SoundBanks/GMBank/gmpiano.txt";
             var file = Application.GetResourceStream(new Uri(bankName, UriKind.Relative));
                String result = "";
                using (var reader = new StreamReader(file.Stream))
                {
                    
                    result += reader.ReadToEnd();
                }
            synth = new StreamSynthesizer(44100, 1, 1000, 2);
          
            CSharpSynth.Banks.BankManager.addBank(new InstrumentBank(44100, bankName));
            int bankfile = BankManager.Count - 1;
            synth.SwitchBank(bankfile);


            byte[] buffer = new byte[synth.BufferSize];
            
            midiSeq = new MidiSequencer(synth);
            midiSeq.LoadMidi("temp.mid", false);
            //System.Diagnostics.Debug.WriteLine("midi file is ");
            //System.Diagnostics.Debug.WriteLine(midifile.OriginalString);
            //midiSeq.LoadMidi(midifile.OriginalString, false);
            midiSeq.Looping = true;
            midiSeq.Play();
            startTime = DateTime.Now;
            ReportOpenMediaCompleted(sourceAttributes, availableStreams);
        }
        protected void CallStreamComplete()
        {
            // This may throw a null reference exception - that indicates that the agent did not correctly
            // subscribe to StreamComplete so it could call NotifyComplete
            if (null != StreamComplete)
            {
                StreamComplete(this, new EventArgs());
            }
        }

        long _currentTimeStamp = 0;
        private Dictionary<MediaSampleAttributeKeys, string> _emptySampleDict = new Dictionary<MediaSampleAttributeKeys, string>();
        protected override void GetSampleAsync(MediaStreamType mediaStreamType)
        {
            //System.Diagnostics.Debug.WriteLine(">>>>>>>>>>>>>>GetSampleAsync.");
            DateTime now = DateTime.Now;
            TimeSpan ts2 = new TimeSpan(now.Ticks);
            TimeSpan ts1 = new TimeSpan(startTime.Ticks);
            TimeSpan ts = ts2.Subtract(ts1);
            System.Diagnostics.Debug.WriteLine("ts minutes " + ts.Minutes);
            if (ts.TotalMinutes >= 60) {
                
                CallStreamComplete();
            }
            byte[] buffer = new byte[synth.BufferSize];
            
            synth.GetNext(buffer);
            using (var stream = new MemoryStream(buffer))
            {
                MediaStreamSample msSamp = new MediaStreamSample(
                    _audioDesc,
                    stream,
                    0,
                    synth.BufferSize,
                    _currentTimeStamp,
                    _emptySampleDict);

                // Move our timestamp and position forward
                _currentTimeStamp += synth.BufferSize * 10000000 / AvgBytesPerSec;

                ReportGetSampleCompleted(msSamp);
            }
        }


        protected override void CloseMedia()
        {
            CallStreamComplete();
        }

        protected override void GetDiagnosticAsync(MediaStreamSourceDiagnosticKind diagnosticKind)
        {
            throw new NotImplementedException();
        }

        protected override void SeekAsync(long seekToTime)
        {
            ReportSeekCompleted(seekToTime);
        }

        protected override void SwitchMediaStreamAsync(MediaStreamDescription mediaStreamDescription)
        {
            throw new NotImplementedException();
        }
    }
}

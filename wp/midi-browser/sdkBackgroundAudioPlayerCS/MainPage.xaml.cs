using CSharpSynth.Banks;
using CSharpSynth.Synthesis;
using Microsoft.Phone.BackgroundAudio;
using Microsoft.Phone.Controls;
/* 
    Copyright (c) 2011 Microsoft Corporation.  All rights reserved.
    Use of this sample source code is subject to the terms of the Microsoft license 
    agreement under which you licensed this sample source code and is provided AS-IS.
    If you did not accept the terms of the license agreement, you are not authorized 
    to use this sample source code.  For the terms of the license, please see the 
    license agreement between you and Microsoft.
  
    To see all Code Samples for Windows Phone, visit http://go.microsoft.com/fwlink/?LinkID=219604 
  
*/
using System;
using System.IO;
using System.Threading;
using System.Windows;
using System.Windows.Navigation;
using CSharpSynth.Sequencer;
using System.Net;
using System.IO.IsolatedStorage;
using System.Windows.Threading;
using Microsoft.Phone.Scheduler;

namespace sdkBackgroundAudioPlayerCS
{
    public partial class MainPage : PhoneApplicationPage
    {
        private String baseUrl = "https://www.duosuccess.com";
        private String midfile = "temp.mid";
        IsolatedStorageFile store;
        private String strUri = "";

        DispatcherTimer trackTimer;
        // Constructor
        public MainPage()
        {
            
            InitializeComponent();
            store = IsolatedStorageFile.GetUserStoreForApplication();

            BackgroundAudioPlayer.Instance.PlayStateChanged += new EventHandler(Instance_PlayStateChanged);
            Browser.LoadCompleted += new LoadCompletedEventHandler(pageLoadComplete);

            Browser.ScriptNotify += new EventHandler<NotifyEventArgs>(midiDetected);


            trackTimer = new DispatcherTimer();
            trackTimer.Interval = TimeSpan.FromSeconds(1);
            trackTimer.Tick += new EventHandler(updateTrackTime);


            _webClient = new WebClient();
            _webClient.OpenReadCompleted += (s1, e1) =>
            {
                if (e1.Error != null)
                {
                    Dispatcher.BeginInvoke(() =>
                    {
                        MessageBox.Show("´íÎó: " + e1.Error.Message);
                    });

                    return;
                }

                bool isSpaceAvailable = IsSpaceIsAvailable(e1.Result.Length);
                if (!isSpaceAvailable)
                {
                    Dispatcher.BeginInvoke(() =>
                    {
                        MessageBox.Show("´íÎó: ¿Õ¼ä²»×ã");
                    });
                    return;
                }
                System.Diagnostics.Debug.WriteLine("writing to " + midfile);
                using (var isfs = new IsolatedStorageFileStream(midfile,
                                               FileMode.CreateNew,
                                               IsolatedStorageFile.GetUserStoreForApplication()))
                {
                    long fileLen = e1.Result.Length;
                    byte[] b = new byte[fileLen];
                    e1.Result.Read(b, 0, b.Length);
                    isfs.Write(b, 0, b.Length);
                    isfs.Flush();
                }
                Dispatcher.BeginInvoke(() =>
                {

                    BackgroundAudioPlayer.Instance.Play();


                    trackTime = TimeSpan.FromSeconds(0);
                    // musicTimer.Start();
                    trackTimer.Start();
                  

                });


            };
        }

        TimeSpan trackTime = TimeSpan.FromSeconds(0);
        Boolean refreshClicked = false;
        private void stopMusic()
        {
            
            System.Diagnostics.Debug.WriteLine("Stop music.");
            trackTimer.Stop();
            removeCache();
            Dispatcher.BeginInvoke(() =>
            {
                TrackTime.Text = "ÒÑÍ£Ö¹";
            });
            BackgroundAudioPlayer.Instance.Stop();
            
            
        }
        private void updateTrackTime(object sender, EventArgs e)
        {
            trackTime = trackTime.Add(TimeSpan.FromSeconds(1));
            Dispatcher.BeginInvoke(() =>
           {

               TrackTime.Text = String.Format("{0:d2}:{1:d2}", trackTime.Minutes, trackTime.Seconds);
           });
        }
        protected override void OnBackKeyPress(System.ComponentModel.CancelEventArgs e)
        {
            if (Browser.CanGoBack)
            {
                Browser.GoBack();
                stopMusic();
            }
            e.Cancel = true;  //Cancels the default behavior.
        }

        private void midiDetected(object sender, NotifyEventArgs e)
        {

            String rawMidi = e.Value.ToString();
            System.Diagnostics.Debug.WriteLine(rawMidi);
            strUri = baseUrl + "/" + rawMidi.Replace(@"../", "");

            Thread downloadThread = new Thread(new ThreadStart(downloadMid));
            downloadThread.Name = "SynthStreamerThread";
            downloadThread.Start();

        }
        WebClient _webClient;
        private void downloadMid()
        {
            System.Diagnostics.Debug.WriteLine("Start download: " + strUri);
            removeCache();
            Dispatcher.BeginInvoke(() =>
            {
                TrackTime.Text = "ÕýÏÂÔØÒôÀÖÎÄ¼þ...";
            });
            _webClient.OpenReadAsync(new Uri(strUri));


        }
        private void removeCache()
        {
            if (store.FileExists(midfile))
            {
                store.DeleteFile(midfile);
                Dispatcher.BeginInvoke(() =>
          {
              TrackTime.Text = "ÒÑÇå³ý»º´æ...";
          });
            }
        }
        Uri currentPage;
        private void pageLoadComplete(object sender, EventArgs e)
        {
            refreshClicked = false;
            System.Diagnostics.Debug.WriteLine("page load complete " + Browser.Source);
            currentPage = Browser.Source;
            Browser.InvokeScript("eval", @"window.getMusic= 
                        function(){
                            var el = document.getElementsByTagName('bgsound')[0];
                            if(el){
                                var midi = el.src;
                                //alert(midi);
                                window.external.notify(midi);
                            };
                         }
                        window.stopmusic = function(){};
                ");
            Browser.InvokeScript("stopmusic");
            Browser.InvokeScript("getMusic");

        }
        private bool IsSpaceIsAvailable(long spaceReq)
        {
            using (var store = IsolatedStorageFile.GetUserStoreForApplication())
            {

                long spaceAvail = store.AvailableFreeSpace;
                if (spaceReq > spaceAvail)
                {
                    return false;
                }
                return true;
            }
        }
        /// <summary>
        /// Checks to see if the BackgroundAudioPlayer is already playing.
        /// Initializes the UI controls accordingly.
        /// </summary>
        /// <param name="e"></param>
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {

        }
        private Boolean isMusicStarted = false;

        /// <summary>
        /// Updates the UI with the current song data.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        void Instance_PlayStateChanged(object sender, EventArgs e)
        {

            System.Diagnostics.Debug.WriteLine("play status changed: " + BackgroundAudioPlayer.Instance.PlayerState);
            switch (BackgroundAudioPlayer.Instance.PlayerState)
            {
                case PlayState.Playing:
                    isMusicStarted = true;
                    if (!trackTimer.IsEnabled) {
                        trackTimer.Start();
                    }
                    break;

                case PlayState.Paused:
                    System.Diagnostics.Debug.WriteLine("isMusicStarted: " + isMusicStarted);
                    if (isMusicStarted)
                    {
                        isMusicStarted = false;
                        if (!refreshClicked)
                        {
                            stopMusic();
                            Browser.Navigate(new Uri(baseUrl));
                        }
                    }
                    
                    break;
            }
        }
        private void OnExit_Click(object sender, RoutedEventArgs e){
            stopMusic();
            BackgroundAudioPlayer.Instance.Stop();
            /*
            while (NavigationService.CanGoBack) {
                NavigationService.RemoveBackEntry();
            }
            this.IsHitTestVisible = this.IsEnabled = false;
            if (this.ApplicationBar != null) {
                foreach (ApplicationBarIconButton item in this.ApplicationBar.Buttons
                    ) {
                    item.IsEnabled = false;
                }
            }*/
        }

        private void OnRefresh_Click(object sender, RoutedEventArgs e)
        {
            refreshClicked = true;
            stopMusic();
            System.Diagnostics.Debug.WriteLine("current page: " + currentPage);
            Browser.Navigate(currentPage);
        }
        private void stopButton_Click(object sender, RoutedEventArgs e)
        {

            

        }

        #region Button Click Event Handlers




        /// <summary>
        /// Tells the background audio agent to play the current 
        /// track or to pause if we're already playing.
        /// </summary>
        /// <param name="sender">The button</param>
        /// <param name="e">Click event args</param>
        private void playButton_Click(object sender, RoutedEventArgs e)
        {
            BackgroundAudioPlayer.Instance.Play();
        }



        #endregion Button Click Event Handlers
    }
}

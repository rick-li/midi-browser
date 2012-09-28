//
//  ViewController.m
//  test
//
//  Created by rick li on 9/24/12.
//  Copyright (c) 2012 duosuccess. All rights reserved.
//
#import "ViewController.h"
#import "Reachability.h"
#import "MBProgressHUD.h"
#import <AVFoundation/AVFoundation.h>
#import <CoreAudio/CoreAudioTypes.h>


@interface ViewController ()

@property (readwrite) AUGraph   processingGraph;
@property (readwrite) AudioUnit samplerUnit;
@property (readwrite) AudioUnit ioUnit;

@end

@implementation ViewController
@synthesize homeBtn;
@synthesize refreshBtn;
@synthesize backBtn;
@synthesize webView;
@synthesize closeBtn;
@synthesize  mySequence;
@synthesize  player;
@synthesize processingGraph     = _processingGraph;
@synthesize samplerUnit         = _samplerUnit;
@synthesize ioUnit              = _ioUnit;


//NSString *url=@"http://duosuccess.com/tcm/001a01080301b01aj.htm";
NSString *homeUrl = @"http://www.duosuccess.com";
//NSString *homeUrl = @"http://rick-li.github.com/android-midi/test2.html";
//NSString *homeUrl = @"http://10.114.191.51/midi/test2.html";

NSString *tmpDir;
NSTimer *timer;

AUGraph _processingGraph;
AudioUnit samplerUnit;

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    webView.delegate=self;
    
    BOOL audioSessionActivated = [self setupAudioSession];
    NSLog(@"audioSessionActivated = %d", audioSessionActivated);
    
    [self createAUGraph];
    OSStatus result = noErr;
    if (self.processingGraph) {
        
        
        //see http://developer.apple.com/library/ios/#documentation/Audio/Conceptual/AudioSessionProgrammingGuide/Cookbook/Cookbook.html
        UInt32 maximumFramesPerSlice = 4096;
        
        AudioUnitSetProperty (
                              self.samplerUnit,
                              kAudioUnitProperty_MaximumFramesPerSlice,
                              kAudioUnitScope_Global,
                              0,                        // global scope always uses element 0
                              &maximumFramesPerSlice,
                              sizeof (maximumFramesPerSlice)
                              );
        // Initialize the audio processing graph.
        result = AUGraphInitialize (self.processingGraph);
        NSAssert (result == noErr, @"Unable to initialze AUGraph object. Error code: %d '%.4s'", (int) result, (const char *)&result);
    }
    // Load the ound font from file
    NSURL *presetURL = [[NSURL alloc] initFileURLWithPath:[[NSBundle mainBundle] pathForResource:
                                                           //@"gorts_mini_piano" ofType:@"sf2"]];
                                                           
                                                           //@"u20" ofType:@"sf2"]];
                                                           @"piano" ofType:@"sf2"]];
    
    
    
    // Initialise the sound font
    //#12 #13
    [self loadFromDLSOrSoundFont: (NSURL *)presetURL withPatch: (int)1];
    
    [self clearCache];
    //enable Zoom
    webView.scalesPageToFit = YES;
    [self loadHome];
 
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(checkNetworkStatus:) name:kReachabilityChangedNotification object:nil];
    
    internetReachable = [Reachability reachabilityForInternetConnection];
    [internetReachable startNotifier];
}

-(void) checkNetworkStatus:(NSNotification *)notice
{
    // called after network status changes
    NetworkStatus internetStatus = [internetReachable currentReachabilityStatus];
    switch (internetStatus)
    {
        case NotReachable:
        {
            NSLog(@"The internet is down, stop Media.");
            [self stopMedia];
            
            break;
        }
    }
}

// Set up the audio session for this app.
- (BOOL) setupAudioSession
{
    NSLog(@"--- setupAudioSession ---");
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    [audioSession setDelegate: self];
    
    //Assign the Playback category to the audio session.
    NSError *audioSessionError = nil;
    [audioSession setCategory: AVAudioSessionCategoryPlayback error: &audioSessionError];
    if (audioSessionError != nil) {NSLog (@"Error setting audio session category."); return NO;}    
    
        
      // Activate the audio session
    [audioSession setActive: YES error: &audioSessionError];
    if (audioSessionError != nil) {NSLog (@"Error activating the audio session."); return NO;}
    
    return YES;
}

- (void)loadHome{
    NSURL *nsURL = [NSURL URLWithString:homeUrl];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:nsURL]; 
    [request setCachePolicy:NSURLRequestReloadIgnoringLocalCacheData];
    [webView loadRequest:request];
}
- (void)clearCache{
    NSError *error = nil;
    tmpDir = [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES)objectAtIndex:0]stringByAppendingPathComponent:@"/File"];
    
    NSLog(@"temp dir is %@", tmpDir);
    BOOL folderExists = [[NSFileManager defaultManager] fileExistsAtPath:tmpDir];
    
    
    if(folderExists){
        NSURL *url = [NSURL URLWithString:tmpDir];
        NSLog(@"URL: %@", url);
        BOOL isRemoved = [[NSFileManager defaultManager] removeItemAtURL:url error: &error];
        
        NSLog (@"Temp Dir removed: %@", isRemoved ? @"YES" : @"NO");
    }

}

- (void)viewDidUnload
{
    [self clearCache];
    [self setRefreshBtn:nil];
    [self setWebView:nil];
    [self setHomeBtn:nil];
    [self setBackBtn:nil];
    [self setCloseBtn:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
}

- (IBAction)refreshAction:(id)sender {
    NSLog(@"refresh");
    [webView reload];
}

- (IBAction)backAction:(id)sender {
    if([webView canGoBack]){
        [webView goBack];
        //TODO if it's already home page, then exit app.
    }
}

- (IBAction)closeAction:(id)sender {
    
}

- (IBAction)homeAction:(id)sender {
    [self loadHome];
}


- (void) playMedia:(NSString *)midPath{
    
    NewMusicSequence(&mySequence);
    NSURL * midiFileURL = [NSURL fileURLWithPath:midPath];
    MusicSequenceFileLoad(mySequence, (__bridge CFURLRef)midiFileURL, 0, kMusicSequenceLoadSMF_ChannelsToTracks);
   
    MusicSequenceSetAUGraph(mySequence, _processingGraph);


    //    MusicTimeStamp lengthInBeats = [self getSequenceLength:mySequence];
    //    NSLog(@"sequence beats is %d", (int)lengthInBeats);
    //    
    //    
    //    Float64 lengthInSeconds = 0;
    //    MusicSequenceGetSecondsForBeats(mySequence, lengthInBeats, &lengthInSeconds);
    //    NSLog(@"sequence length is %d", (int)lengthInSeconds);
    
    [self setLoop:mySequence];
    [self doStartMidi];
    
}

- (void)doStartMidi{
    NSLog(@"do start midi");
    
    NewMusicPlayer(&player);
    MusicPlayerSetSequence(player, mySequence);
    MusicPlayerPreroll(player);
    MusicPlayerStart(player);
    
}

- (void)setLoop:(MusicSequence)sequence {
    UInt32 tracks;
    
    
    if (MusicSequenceGetTrackCount(sequence, &tracks) != noErr)
        
        
        for (UInt32 i = 0; i < tracks; i++) {
            MusicTrack track = NULL;
            MusicTimeStamp trackLen = 0;
            
            UInt32 trackLenLen = sizeof(trackLen);
            
            MusicSequenceGetIndTrack(sequence, i, &track);
            
            MusicTrackGetProperty(track, kSequenceTrackProperty_TrackLength, &trackLen, &trackLenLen);
            MusicTrackLoopInfo loopInfo = { trackLen, 0 };
            MusicTrackSetProperty(track, kSequenceTrackProperty_LoopInfo, &loopInfo, sizeof(loopInfo));
            NSLog(@"track length is %f", trackLen);
        }
    
    
}

- (void) stopMedia{
    
    if(player == nil ){
        return;
    }
    Boolean isPlaying = FALSE;
    MusicPlayerIsPlaying(player, &isPlaying);
    if(!isPlaying){
        NSLog(@"not playing music, no need to stop.");
        return;
    }
    
    OSStatus result = noErr;
    
    result = MusicPlayerStop(player);
    
    UInt32 trackCount;
    MusicSequenceGetTrackCount(mySequence, &trackCount);
    
    MusicTrack track;
    for(int i=0;i<trackCount;i++)
    {
        MusicSequenceGetIndTrack (mySequence,0,&track);
        result = MusicSequenceDisposeTrack(mySequence, track);
        
    }
    
    result = DisposeMusicPlayer(player);
    result = DisposeMusicSequence(mySequence);
//    result = DisposeAUGraph(_processingGraph);

    NSLog(@"Stopping media, status is %@", result);
}


- (void) webViewDidFinishLoad:(UIWebView *)webView{
    NSLog(@"webview load finished");
    //    NSString *strjs = @"document.queryBySelector('embed').src";
    NSString *strjs = @"document.querySelector('embed').src";
    NSString *midUrl = [webView stringByEvaluatingJavaScriptFromString:strjs];
    
    //remove mask
[MBProgressHUD hideHUDForView:self.view animated:YES];
    NSLog(@"mid Url is %@", midUrl);
    
    if(!midUrl || [midUrl length]==0){
        NSLog(@"no midi in this page, don't play music");
        return;
    }
    NSError *error = nil;
    if (![[NSFileManager defaultManager] fileExistsAtPath:tmpDir])
        [[NSFileManager defaultManager] createDirectoryAtPath:tmpDir withIntermediateDirectories:NO attributes:nil error:&error];
    
    //download midi
    NSURL *url = [NSURL URLWithString:
                  midUrl];
    NSData *data = [NSData dataWithContentsOfURL:url];
    if(data)
    {
        NSString *midPath = [tmpDir stringByAppendingPathComponent:[url lastPathComponent]];
        [data writeToFile:midPath atomically:YES];
        NSLog(@"midi file path is %@", midPath);
        [self playMedia:midPath];
    }
    
  
}


- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType{
    NSLog(@"Loading %@", [request URL]);
    [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self stopMedia];
    return YES;
}



-(OSStatus) loadFromDLSOrSoundFont: (NSURL *)bankURL withPatch: (int)presetNumber {
    
    OSStatus result = noErr;
    
    // fill out a bank preset data structure
    AUSamplerBankPresetData bpdata;
    bpdata.bankURL  = (__bridge CFURLRef) bankURL;
    bpdata.bankMSB  = kAUSampler_DefaultMelodicBankMSB;
    bpdata.bankLSB  = kAUSampler_DefaultBankLSB;
    bpdata.presetID = (UInt8) presetNumber;
    
    
    
    // set the kAUSamplerProperty_LoadPresetFromBank property
    result = AudioUnitSetProperty(self.samplerUnit,
                                  kAUSamplerProperty_LoadPresetFromBank,
                                  kAudioUnitScope_Global,
                                  0,
                                  &bpdata,
                                  sizeof(bpdata));
    

    
    // check for errors
    NSCAssert (result == noErr,
               @"Unable to set the preset property on the Sampler. Error code:%d '%.4s'",
               (int) result,
               (const char *)&result);
    
    return result;
}



- (BOOL) createAUGraph {
    
    // Each core audio call returns an OSStatus. This means that we
    // Can see if there have been any errors in the setup
	OSStatus result = noErr;
    
    // Create 2 audio units one sampler and one IO
	AUNode samplerNode, ioNode;
    
    // Specify the common portion of an audio unit's identify, used for both audio units
    // in the graph.
    // Setup the manufacturer - in this case Apple
	AudioComponentDescription cd = {};
	cd.componentManufacturer     = kAudioUnitManufacturer_Apple;
    
    // Instantiate an audio processing graph
	result = NewAUGraph (&_processingGraph);
    NSCAssert (result == noErr, @"Unable to create an AUGraph object. Error code: %d '%.4s'", (int) result, (const char *)&result);
    
	//Specify the Sampler unit, to be used as the first node of the graph
	cd.componentType = kAudioUnitType_MusicDevice; // type - music device
	cd.componentSubType = kAudioUnitSubType_Sampler; // sub type - sampler to convert our MIDI
	
    // Add the Sampler unit node to the graph
	result = AUGraphAddNode (self.processingGraph, &cd, &samplerNode);
    NSCAssert (result == noErr, @"Unable to add the Sampler unit to the audio processing graph. Error code: %d '%.4s'", (int) result, (const char *)&result);
    
	// Specify the Output unit, to be used as the second and final node of the graph	
	cd.componentType = kAudioUnitType_Output;  // Output
	cd.componentSubType = kAudioUnitSubType_RemoteIO;  // Output to speakers
    
    // Add the Output unit node to the graph
	result = AUGraphAddNode (self.processingGraph, &cd, &ioNode);
    NSCAssert (result == noErr, @"Unable to add the Output unit to the audio processing graph. Error code: %d '%.4s'", (int) result, (const char *)&result);
    
    // Open the graph
	result = AUGraphOpen (self.processingGraph);
    NSCAssert (result == noErr, @"Unable to open the audio processing graph. Error code: %d '%.4s'", (int) result, (const char *)&result);
    
    // Connect the Sampler unit to the output unit
	result = AUGraphConnectNodeInput (self.processingGraph, samplerNode, 0, ioNode, 0);
    NSCAssert (result == noErr, @"Unable to interconnect the nodes in the audio processing graph. Error code: %d '%.4s'", (int) result, (const char *)&result);
    
	// Obtain a reference to the Sampler unit from its node
	result = AUGraphNodeInfo (self.processingGraph, samplerNode, 0, &_samplerUnit);
    NSCAssert (result == noErr, @"Unable to obtain a reference to the Sampler unit. Error code: %d '%.4s'", (int) result, (const char *)&result);
    
	// Obtain a reference to the I/O unit from its node
	result = AUGraphNodeInfo (self.processingGraph, ioNode, 0, &_ioUnit);
    NSCAssert (result == noErr, @"Unable to obtain a reference to the I/O unit. Error code: %d '%.4s'", (int) result, (const char *)&result);
    
    return YES;
}

@end

#include "midilib_mac.h"
#include <QtCore>
#include <Cocoa/Cocoa.h>
#include <CoreAudio/CoreAudio.h>
#include <AudioUnit/AudioUnit.h>
#include <AudioToolbox/AUGraph.h>
#include <CoreMIDI/MIDIServices.h>
#include <AudioToolbox/MusicPlayer.h>
#import <AudioToolbox/MusicPlayer.h>
#import <AVFoundation/AVFoundation.h>
#import <CoreAudio/CoreAudioTypes.h>

enum {
    kMidiMessage_ControlChange 		= 0xB,
    kMidiMessage_ProgramChange 		= 0xC,
    kMidiMessage_BankMSBControl 	= 0,
    kMidiMessage_BankLSBControl		= 32,
    kMidiMessage_NoteOn 			= 0x9
};
midilib::midilib()
{

}

void midilib::play(const QString* midifile)
{
    //QString midifile = "hello";
    //qDebug() << midifile->toUtf8().data();
    QString m = *midifile;
    NSURL* url = [NSURL URLWithString:
            [NSString stringWithUTF8String: m.toUtf8().data()]];
    //qDebug() << "before end" << url;
    qDebug() << [[url absoluteString] UTF8String];
    qDebug() << "end";
    initAUGraph();
    //testAudio();
    try{
        NewMusicSequence(&mySequence);
    }catch(std::exception& e){
        qDebug() << "exception caught:: " << e.what() << '\n';

    }
    MusicSequenceFileLoad(mySequence, (CFURLRef)url, 0, kMusicSequenceLoadSMF_ChannelsToTracks);
    MusicSequenceSetAUGraph(mySequence, processingGraph);
    setLoop(mySequence);
    startPlayer();
}
void midilib::stop()
{
    if(player == 0 ){
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
    qDebug() << "Stop music result: " << result;
}
void midilib::startPlayer(){
    qDebug() << "Start player.";
    NewMusicPlayer(&player);
    MusicPlayerSetSequence(player, mySequence);

    MusicPlayerPreroll(player);
    MusicPlayerStart(player);

}
void midilib::setLoop(MusicSequence sequence){
    UInt32 tracks;


    if (MusicSequenceGetTrackCount(sequence, &tracks) != noErr){
        qDebug() << "track size is " <<  (int)tracks;
    }

    for (UInt32 i = 0; i < tracks; i++) {
        MusicTrack track = NULL;
        MusicTimeStamp trackLen = 0;

        UInt32 trackLenLen = sizeof(trackLen);

        MusicSequenceGetIndTrack(sequence, i, &track);

        MusicTrackGetProperty(track, kSequenceTrackProperty_TrackLength, &trackLen, &trackLenLen);
        MusicTrackLoopInfo loopInfo = { trackLen, 0 };
        MusicTrackSetProperty(track, kSequenceTrackProperty_LoopInfo, &loopInfo, sizeof(loopInfo));
        qDebug() << "track length is " << trackLen;
    }

}
void midilib::initAUGraph(){
    OSStatus result = noErr;
    AUNode samplerNode, ioNode;
    result = NewAUGraph (&processingGraph);
    AudioComponentDescription cd = {};
    cd.componentManufacturer     = kAudioUnitManufacturer_Apple;
    cd.componentType = kAudioUnitType_MusicDevice;
    cd.componentSubType = kAudioUnitSubType_DLSSynth;
    result = AUGraphAddNode (processingGraph, &cd, &samplerNode);
    cd.componentType = kAudioUnitType_Output;  // Output
    cd.componentSubType = kAudioUnitSubType_DefaultOutput;  // Output to speakers

    // Add the Output unit node to the graph
    result = AUGraphAddNode (processingGraph, &cd, &ioNode);
    result = AUGraphOpen (processingGraph);
    result = AUGraphConnectNodeInput (processingGraph, samplerNode, 0, ioNode, 0);
    result = AUGraphNodeInfo (processingGraph, samplerNode, 0, &samplerUnit);
    result = AUGraphNodeInfo (processingGraph, ioNode, 0, &ioUnit);

    UInt32 maximumFramesPerSlice = 4096;

    AudioUnitSetProperty (
                samplerUnit,
                kAudioUnitProperty_MaximumFramesPerSlice,
                kAudioUnitScope_Global,
                0,                        // global scope always uses element 0
                &maximumFramesPerSlice,
                sizeof (maximumFramesPerSlice)
                );

    if (processingGraph) {

        //NSLog(@"initialize audio process graph");
        // Initialize the audio processing graph.
        result = AUGraphInitialize (processingGraph);
        AUGraphStart(processingGraph);
        //      CAShow (processingGraph);
    }


}
void midilib::testAudio(){

    UInt8 midiChannelInUse = 0;
    // we're going to play an octave of MIDI notes: one a second
    for (int i = 0; i < 13; i++) {
        UInt32 noteNum = i + 60;
        UInt32 onVelocity = 127;
        UInt32 noteOnCommand = 	kMidiMessage_NoteOn << 4 | midiChannelInUse;

        NSLog (@"Playing Note: Status: 0x%u, Note: %u ", noteOnCommand, noteNum);

        MusicDeviceMIDIEvent(samplerUnit, noteOnCommand, noteNum, onVelocity, 0);

        // sleep for a second
        usleep (1 * 1000 * 1000);

        MusicDeviceMIDIEvent(samplerUnit, noteOnCommand, noteNum, 0, 0);
    }


}



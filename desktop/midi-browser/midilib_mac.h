#include "QtCore"

#include <CoreAudio/CoreAudio.h>
#include <AudioUnit/AudioUnit.h>
#include <AudioToolbox/AUGraph.h>
#include <CoreMIDI/MIDIServices.h>
#include <AudioToolbox/MusicPlayer.h>

#ifndef MIDILIB_MAC_H
#define MIDILIB_MAC_H

class midilib
{
public:
    midilib();
    void play(const QString* midifile );
    void stop();

private:
    AUNode synthNode;
    AUNode filterNode;
    AUNode outputNode;
    AUGraph   processingGraph;
    AudioUnit samplerUnit;
    AudioUnit ioUnit;
    MusicSequence mySequence;
    MusicPlayer player;

    void initAUGraph();
    void setLoop(MusicSequence sequence);
    void startPlayer();
    void testAudio();
};

#endif // MIDILIB_MAC_H

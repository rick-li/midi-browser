#-------------------------------------------------
#
# Project created by QtCreator 2013-03-20T11:03:02
#
#-------------------------------------------------

QT       += webkitwidgets network widgets

TARGET = midi-browser
TEMPLATE = app


SOURCES += main.cpp\
        mainwindow.cpp

HEADERS  += mainwindow.h \
    midilib_mac.h

mac {
    ICON = duo.icns
    #ICON = browser.icns
    QMAKE_INFO_PLIST = Info_mac.plist
    OBJECTIVE_SOURCES += midilib_mac.mm
    LIBS +=-framework Cocoa\
    -framework AppKit\
     -framework CoreAudio\
     -framework CoreMIDI -framework AudioToolbox\
     -framework AudioUnit -framework CoreData

}
EXAMPLE_FILES = Info_mac.plist browser.icns

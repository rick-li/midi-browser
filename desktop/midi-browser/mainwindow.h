
#include <QtWidgets>
#include <QNetworkAccessManager>
#include <QNetworkRequest>
#include <QNetworkReply>

#include "midilib_mac.h"

class QWebView;
QT_BEGIN_NAMESPACE
class QLineEdit;
QT_END_NAMESPACE

//! [1]
class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    MainWindow();
    ~MainWindow();

public slots:

    void adjustLocation();
    void changeLocation();
    void adjustTitle();
    void setProgress(int p);
    void finishLoading(bool);
    void downloadFinished(QNetworkReply *reply);
    void stopMusic();

private:
    QString jQuery;
    QWebView *view;
    QAction *stopAction;
    QLineEdit *locationEdit;
    midilib midilib;
    QString midiFilePath;
    int progress;
    QNetworkAccessManager manager;

    void playMusic(QFile& file);
    void clearCache();
//! [1]
};

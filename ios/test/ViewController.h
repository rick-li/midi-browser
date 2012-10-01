//
//  ViewController.h
//  test
//
//  Created by rick li on 9/24/12.
//  Copyright (c) 2012 duosuccess. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AudioToolbox/MusicPlayer.h>

@class Reachability;

@interface ViewController : UIViewController
{
    Reachability* internetReachable;
}
@property (weak, nonatomic) IBOutlet UIBarButtonItem *homeBtn;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *refreshBtn;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *backBtn;
@property (weak, nonatomic) IBOutlet UIWebView *webView;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *closeBtn;


@property MusicSequence mySequence;
@property MusicPlayer player;

-(void) checkNetworkStatus:(NSNotification *)notice;

- (IBAction)refreshAction:(id)sender;
- (IBAction)backAction:(id)sender;

- (IBAction)homeAction:(id)sender;

- (void)loadHome;
- (void)stopMedia;
@end

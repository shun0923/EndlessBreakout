import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class EndlessBreakout extends PApplet {


Minim minim;
AudioSnippet boundSound;
AudioSnippet breakSound;
AudioSnippet powerUpSound;
AudioSnippet powerDownSound;
AudioSnippet startSound;
AudioSnippet clearSound;
AudioSnippet gameOverSound;

int ballRad = 10;
int ballSpeedMin = 10;
int ballSpeedMax = 20;
ArrayList<Float> ballX = new ArrayList<Float>();
ArrayList<Float> ballY = new ArrayList<Float>();
ArrayList<Float> ballDegree = new ArrayList<Float>();//右方向0 反時計回り
ArrayList<Float> ballSpeedNow = new ArrayList<Float>();
ArrayList<Integer> ballType = new ArrayList<Integer>();
int ballNumber = 0;
boolean collideBallP = false;

int playerWidthStandard = 100;
int playerWidth;
int playerHeight = 20;
float playerX;
int playerY;
int playerSpeedStandard = 8;
int playerSpeed;
float longCount = -1;
float shortCount = -1;
float slowCount = -1;
float fastCount = -1;

int blockWidth;
int blockHeight;
int blockNumberW = 10;
int blockNumberH = 10;
int startBlockX;
int startBlockY;
boolean existBlock[][] = new boolean[blockNumberW][blockNumberH];
int existBlockNumber = blockNumberW * blockNumberH;
float deleteBlockTime[][] = new float[blockNumberW][blockNumberH];
float blockSize[][] = new float[blockNumberW][blockNumberH];
int blockType[][] = new int[blockNumberW][blockNumberH];
float blockBorn[][] = new float[blockNumberW][blockNumberH];

boolean pressRight = false;
boolean pressLeft = false;
boolean pressSpace = false;

int score;
float count;
long countL;
int countTime = 200;
float startCount;
boolean startText = true;
boolean clear = false;

public void setup() {
	minim = new Minim(this);
	boundSound = minim.loadSnippet("bound.mp3");
	breakSound = minim.loadSnippet("break.mp3");
	powerUpSound = minim.loadSnippet("powerUp.mp3");
	powerDownSound = minim.loadSnippet("powerDown.mp3");
	startSound = minim.loadSnippet("start.mp3");
	clearSound = minim.loadSnippet("clear.mp3");
	gameOverSound = minim.loadSnippet("gameOver.mp3");
	
	start();
	startBlockX = (int) (width * 0.05f);
	startBlockY = (int) (height * 0.1f);
	blockWidth = (int) ((width - startBlockX * 2) / blockNumberW);
	blockHeight = (int) (height * 0.25f / blockNumberH);
	noLoop();
}

public void start() {
	ballX.clear();
	ballX.add(width * 0.5f);
	ballY.clear();
	ballY.add(height * 0.8f);
	ballSpeedNow.clear();
	ballSpeedNow.add(10.0f);
	ballDegree.clear();
	ballDegree.add(random(60, 120));
	ballType.clear();
	ballType.add(0);
	playerWidth = playerWidthStandard;
	playerSpeed = playerSpeedStandard;
	playerX = width * 0.5f;
	playerY = round(height * 0.9f);
	longCount = -1;
	shortCount = -1;
	slowCount = -1;
	fastCount = -1;
	score = 0;
	count = 0;
	countL = 100;
	startCount = millis() / 1000.0f;

	for(int i = 0; i < blockNumberH; i ++) {
		for(int j = 0; j < blockNumberW; j ++) {
			existBlock[j][i] = true;
			deleteBlockTime[j][i] = -1;
			blockSize[j][i] = 1;
			blockType[j][i] = 0;
			blockBorn[j][i] = 1 + random(100) / 100.0f;
		}
	}
}

public void stop() {
	boundSound.close();
	breakSound.close();
	powerUpSound.close();
	powerDownSound.close();
	startSound.close();
	clearSound.close();
	gameOverSound.close();
	minim.stop();
	super.stop();
}

public void draw() {
	existBlockNumber = 0;
	for(int i = 0; i < blockNumberH; i ++) {
		for(int j = 0; j < blockNumberW; j ++) {
			if(existBlock[j][i]) {
				existBlockNumber ++;
			}
		}
	}

	background(0, 0, 0);
	for(ballNumber = 0; ballNumber < ballX.size(); ballNumber ++) {
		calBallCollide();
		drawBall();
		if((ballY.get(ballNumber) >= height - ballRad) || (collideBallP && ballType.get(ballNumber) > 0)) {
			ballX.remove(ballNumber);
			ballY.remove(ballNumber);
			ballDegree.remove(ballNumber);
			ballSpeedNow.remove(ballNumber);
			ballType.remove(ballNumber);
			ballNumber --;
		}else {
			for(int ballNumber2 = ballNumber + 1; ballNumber2 < ballX.size(); ballNumber2 ++) {
				if(dist(ballX.get(ballNumber), ballY.get(ballNumber), ballX.get(ballNumber2), ballY.get(ballNumber2)) <= ballRad * 2) {
					ballX.add(ballX.get(ballNumber2));
					ballX.remove(ballNumber2);
					ballX.remove(ballNumber);
					ballY.add(ballY.get(ballNumber2));
					ballY.remove(ballNumber2);
					ballY.remove(ballNumber);
					ballDegree.add(ballDegree.get(ballNumber2));
					ballDegree.remove(ballNumber2);
					ballDegree.remove(ballNumber);
					ballSpeedNow.add(ballSpeedNow.get(ballNumber2));
					ballSpeedNow.remove(ballNumber2);
					ballSpeedNow.remove(ballNumber);
					ballType.add(min(ballType.get(ballNumber), ballType.get(ballNumber2)));
					ballType.remove(ballNumber2);
					ballType.remove(ballNumber);
					ballNumber --;
					break;
				}
			}
		}
	}
	drawBlock();
	if(longCount > 0 && longCount >= shortCount) {
		if(countL + count / countTime - longCount < 2) {
			playerWidth = playerWidthStandard * 2;
		}else {
			longCount = -1;
			shortCount = -1;
			playerWidth = playerWidthStandard;
		}
	}
	if(shortCount > 0 && longCount < shortCount) {
		if(countL + count / countTime - shortCount < 1) {
			playerWidth = round(playerWidthStandard * 0.7f);
		}else {
			longCount = -1;
			shortCount = -1;
			playerWidth = playerWidthStandard;
		}
	}
	if(fastCount > 0 && fastCount >= slowCount) {
		if(countL + count / countTime - fastCount < 2) {
			playerSpeed = round(playerSpeedStandard * 2);
		}else {
			fastCount = -1;
			slowCount = -1;
			playerSpeed = playerSpeedStandard;
		}
	}
	if(slowCount > 0 && fastCount < slowCount) {
		if(countL + count / countTime - slowCount < 1) {
			playerSpeed = round(playerSpeedStandard * 0.5f);
		}else {
			fastCount = -1;
			slowCount = -1;
			playerSpeed = playerSpeedStandard;
		}
	}
	drawPlayer();

	count ++;
	if(count >= countTime) {
		count = 0;
		countL ++;
	}
	if(startText) {
		fill(255);
		textSize(48);
		textAlign(CENTER, CENTER);
		text("PRESS SPACE BAR!", width * 0.5f, height * 0.5f);
		startText = false;
	}
	clear = existBlockNumber == 0;
	if(clear) {
		score *= 10;
		fill(255);
		textSize(96);
		textAlign(CENTER, CENTER);
		text("GAME CLEAR!", width * 0.5f, height * 0.5f);
		clearSound.play();
		clearSound.rewind();
		noLoop();
	}
	if(ballX.size() <= 0) {
		fill(255);
		textSize(96);
		textAlign(CENTER, CENTER);
		text("GAME OVER!", width * 0.5f, height * 0.5f);
		gameOverSound.play();
		gameOverSound.rewind();
		noLoop();
	}
	drawScore();
}

public void calBallCollide() {
	if(ballX.get(ballNumber) <= ballRad) {
		collideRL(ballX.get(ballNumber) - 10);
		boundSound.play();
		boundSound.rewind();
	}
	if(ballY.get(ballNumber) <= ballRad) {
		collideUD(ballY.get(ballNumber) - 10);
		boundSound.play();
		boundSound.rewind();
	}
	if(ballX.get(ballNumber) >= width - ballRad) {
		collideRL(ballX.get(ballNumber) + 10);
		boundSound.play();
		boundSound.rewind();
	}

	collideBallP = false;
	CBCforRect(playerX, playerY, playerWidth, playerHeight, true, -1, -1);

	for(int k = 0; k < blockNumberH; k ++) {
		for(int j = 0; j < blockNumberW; j ++) {
			if(existBlock[j][k]) {
				CBCforRect(startBlockX + (j + 0.5f) * blockWidth, startBlockY + (k + 0.5f) * blockHeight, round(blockWidth * blockSize[j][k]), round(blockHeight * blockSize[j][k]), false, j, k);
			}
		}
	}
}

public void CBCforRect(float opponentX, float opponentY, int opponentWidth, int opponentHeight, boolean isPlayer, int j, int i) {
	boolean collideBall = false;
	if((ballX.get(ballNumber) >= (opponentX - opponentWidth * 0.5f) && ballX.get(ballNumber) <= (opponentX + opponentWidth * 0.5f))
		&& ballY.get(ballNumber) >= (opponentY - ballRad - opponentHeight * 0.5f) && ballY.get(ballNumber) <= (opponentY + ballRad + opponentHeight * 0.5f)) {
		if(isPlayer) {
			collideUDforP();
		}else {
			collideUD(opponentY);
			existBlock[j][i] = false;
		}
		collideBall = true;
	}
	if((ballX.get(ballNumber) >= (opponentX - ballRad - opponentWidth * 0.5f) && ballX.get(ballNumber) <= (opponentX + ballRad + opponentWidth * 0.5f))
		&& ballY.get(ballNumber) >= (opponentY - opponentHeight * 0.5f) && ballY.get(ballNumber) <= (opponentY + opponentHeight * 0.5f)) {
		collideRL(opponentX);
		if(!isPlayer) {
			existBlock[j][i] = false;
		}
		collideBall = true;
	}
	if(isPlayer) {
		if(dist(ballX.get(ballNumber), ballY.get(ballNumber), opponentX + opponentWidth * 0.5f * signum(ballX.get(ballNumber) - opponentX), opponentY + opponentHeight * 0.5f * signum(ballY.get(ballNumber) - opponentY)) <= ballRad) {
			if(ballX.get(ballNumber) - opponentX - opponentWidth * 0.5f * signum(ballX.get(ballNumber) - opponentX) < ballY.get(ballNumber) - opponentY - opponentHeight * 0.5f * signum(ballY.get(ballNumber) - opponentY)) {
				collideUDforP();
			}else {
				collideRL(opponentX);
			}
			collideBall = true;
		}
	}
	if(!isPlayer && !existBlock[j][i]) {
		deleteBlockTime[j][i] = countL + count / countTime;
		blockSize[j][i] = 0;
		score += min(i + 1, blockNumberW - i) * (2 - (float) existBlockNumber / (blockNumberH * blockNumberW)) * (100.0f / countL) * 50;
		if(blockType[j][i] > 0) {
				ballX.add(opponentX);
				ballY.add(opponentY);
				ballSpeedNow.add(10.0f);
				ballDegree.add(random(360));
				ballType.add(blockType[j][i] - 1);
		}
		breakSound.play();
		breakSound.rewind();
	}
	if(isPlayer && collideBall) {
		collideBallP = true;
		ballSpeedNow.set(ballNumber, constrain(ballSpeedMin * (1 + abs(ballX.get(ballNumber) - playerX) / playerWidth), ballSpeedMin, ballSpeedMax));
		switch(ballType.get(ballNumber)) {
			case 1 :
				longCount = countL + count / countTime;
				powerUpSound.play();
				powerUpSound.rewind();
				break;
			case 2 :
				shortCount = countL + count / countTime;
				powerDownSound.play();
				powerDownSound.rewind();
				break;
			case 3 :
				fastCount = countL + count / countTime;
				powerUpSound.play();
				powerUpSound.rewind();
				break;
			case 4 :
				slowCount = countL + count / countTime;
				powerDownSound.play();
				powerDownSound.rewind();
				break;
			default :
				boundSound.play();
				boundSound.rewind();
				break;
		}
	}
}

public void collideRL(float opponentX) {
	ballDegree.set(ballNumber, (ballDegree.get(ballNumber) + 90) % 360 - 180);
	ballDegree.set(ballNumber, (360 + 90 + abs(ballDegree.get(ballNumber)) * signum(opponentX - ballX.get(ballNumber))) % 360);
}
public void collideUD(float opponentY) {
	ballDegree.set(ballNumber, (ballDegree.get(ballNumber) + 180) % 360 - 180);
	ballDegree.set(ballNumber, (360 + abs(ballDegree.get(ballNumber)) * signum(opponentY - ballY.get(ballNumber))) % 360);
}
public void collideUDforP() {
	ballDegree.set(ballNumber, (ballDegree.get(ballNumber) + 180) % 360 - 180);
	ballDegree.set(ballNumber, abs(ballDegree.get(ballNumber)));
	if((playerX - playerWidth * 0.25f) > ballX.get(ballNumber) || (playerX < ballX.get(ballNumber) && (playerX + playerWidth * 0.25f) > ballX.get(ballNumber))) {
		ballDegree.set(ballNumber, 180 - (180 - ballDegree.get(ballNumber)) * (1 - abs(playerX - ballX.get(ballNumber)) / playerWidth));
	}else {
		ballDegree.set(ballNumber, ballDegree.get(ballNumber) * (1 - abs(playerX - ballX.get(ballNumber)) / playerWidth));
	}
	ballDegree.set(ballNumber, (360 + ballDegree.get(ballNumber) * signum(playerY - ballY.get(ballNumber))) % 360);
}


public void drawBlock() {
	rectMode(CENTER);
	for(int i = 0; i < blockNumberH; i ++) {
		for(int j = 0; j < blockNumberW; j ++) {
			switch(blockType[j][i]) {
				case 1 :
					fill(255, 0, 0);
					break;
				case 2 :
					fill(255, 0, 255);
					break;
				case 3 :
					fill(150, 0, 150);
					break;
				case 4 :
					fill(255, 255, 255);
					break;
				case 5 :
					fill(150, 150, 150);
					break;
				default :
					fill(0, 255 * i / blockNumberH, 255 - 255 * i / blockNumberH);
					break;
			}
			if(existBlock[j][i]) {
				blockSize[j][i] = min(1, blockSize[j][i] + 0.01f);
				rect(startBlockX + (j + 0.5f) * blockWidth, startBlockY + (i + 0.5f) * blockHeight, round(blockWidth * blockSize[j][i]), round(blockHeight * blockSize[j][i]));
			}else if(countL + count / countTime - deleteBlockTime[j][i] >= blockBorn[j][i]) {
				blockBorn[j][i] += random(600) / 100.0f + 1;
				deleteBlockTime[j][i] = -1;
				existBlock[j][i] = true;
				float randomBlock = random(100);
				if(randomBlock < 15) {
					blockType[j][i] = 1;
				}else if(randomBlock < 20) {
					blockType[j][i] = 2;
				}else if(randomBlock < 25) {
					blockType[j][i] = 3;
				}else if(randomBlock < 30) {
					blockType[j][i] = 4;
				}else if(randomBlock < 35) {
					blockType[j][i] = 5;
				}else {
					blockType[j][i] = 0;
				}
			}
		}
	}
}

public void drawBall() {
	ballX.set(ballNumber, ballX.get(ballNumber) + ballSpeedNow.get(ballNumber) * cos(radians(ballDegree.get(ballNumber))));
	ballY.set(ballNumber, ballY.get(ballNumber) - ballSpeedNow.get(ballNumber) * sin(radians(ballDegree.get(ballNumber))));
	ellipseMode(CENTER);
	switch(ballType.get(ballNumber)) {
		case 1 :
			fill(255, 0, 255);
			break;
		case 2 :
			fill(150, 0, 150);
			break;
		case 3 :
			fill(255, 255, 255);
			break;
		case 4 :
			fill(150, 150, 150);
			break;
		default :
			fill(255, 255, 0);
			break;
	}
	ellipse(round(ballX.get(ballNumber)), round(ballY.get(ballNumber)), ballRad * 2, ballRad * 2);
}

public void drawPlayer() {
	if(keyPressed) {
		if(pressRight) {
			playerX += playerSpeed;
		}else if(pressLeft) {
			playerX -= playerSpeed;
		}
	}
	playerX = constrain(playerX, 0 - playerWidth * 0.4f, width + playerWidth * 0.4f);
	rectMode(CENTER);
	fill(255, 0, 100);
	rect(round(playerX), playerY, playerWidth, playerHeight);
}

public void drawScore() {
	fill(255);
	textSize(30);
	textAlign(LEFT, TOP);
	text("SCORE : "+score+"\nTIME : "+(round((millis() / 1000.0f - startCount) * 10.0f) / 10.0f), 10, 10);
}

public void keyPressed() {
	pressRight = keyCode == RIGHT;
	pressLeft = keyCode == LEFT;
	pressSpace = key == ' ';
	if(pressSpace) {
		start();
		startSound.play();
		startSound.rewind();
		loop();
	}
}

public void keyReleased() {
	pressRight = keyCode != RIGHT && pressRight;
	pressLeft = keyCode != LEFT && pressLeft;
	pressSpace = !(key == ' ');
}

public int signum(float x) {
	return x > 0 ? 1
		: x < 0 ? -1 : 0;
}
  public void settings() { 	size(800, 1000); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "EndlessBreakout" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

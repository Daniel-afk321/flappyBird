package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;


public class MyGdxGame extends ApplicationAdapter {
	//variaveis
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTope;
	private Texture ouro;
	private Texture prata;
	private Texture gameOver;
	private Texture Logo;

	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;
	private Circle Pao;

	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade	=2;
	private float posicaoInicialVerticalPassaro=0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float posicaoPaoHorizontal;
	private float posicaoPaoVertical;
	private  float espacoEntreCanos;
	private Random random;
	private  int pontos=0;
	private  int pontuacaoMaxima=0;
	private  boolean passouCano=false;
	private int estadoJogo = 0;
	private  float posicaoHorizontalPassaro = 0;

	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;
	Sound Paoo;

	Preferences preferencias;

	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;



	@Override
	public void create () {
		inicializarTexturas();
		inicializaObjetos();
	}

	@Override
	public void render () {

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();

	}
	//esta inicializando as texturas utilizadas no jogo
	private void inicializarTexturas()
	{
		passaros = new Texture[3];
		passaros[0] = new Texture("pombo1.png");
		passaros[1] = new Texture("pombo2.png");
		passaros[2] = new Texture("pombo3.png");

		fundo = new Texture("fundo2.png");
		ouro = new Texture("pao_ouro.png");
		prata = new Texture("pao_prata.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTope = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
		Logo = new Texture("spash.png");
	}
	//esta iniciando os objetos, como a posição inicial vertical do pássaro, a posição horizontal do cano, o espaço entre os canos etc...
	private void inicializaObjetos()
	{
		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;

		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);

		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();
		Pao = new Circle();

		somVoando = Gdx.audio.newSound( Gdx.files.internal("som_asa.wav"));
		somColisao  = Gdx.audio.newSound( Gdx.files.internal("som_batida.wav"));
		somPontuacao  = Gdx.audio.newSound( Gdx.files.internal("som_pontos.wav"));
		Paoo  = Gdx.audio.newSound( Gdx.files.internal("minecraft-eating_Npa05nZO.wav"));

		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

	}
	//Esta verificando o estado atual do jogo, caso o estado esteja no 0 o jogador deve toca na tela para o jogo iniciar, ja no estado 1 ele esta se movendo, no estado 2 significa que o jogador morreu, ele deve tocar na tela para reiniciar
	private void verificarEstadoJogo()
	{
		boolean toqueTela = Gdx.input.justTouched();
		if(estadoJogo == 0)
		{
			if(toqueTela)
			{
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
		}else if (estadoJogo == 1)
		{
			if(toqueTela)
			{
				gravidade = -15;
				somVoando.play();

			}
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			if(posicaoCanoHorizontal < -canoTope.getWidth() )
			{
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
			}

			if(posicaoInicialVerticalPassaro > 0 || toqueTela)

				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
			gravidade++;
		}else if (estadoJogo == 2)
		{
			if (pontos > pontuacaoMaxima)
			{
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferencias.flush();
			}
			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime()*500;

			if (toqueTela)
			{
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo / 2;
				posicaoCanoHorizontal = larguraDispositivo;
			}
		}

	}
	//esta verificando a colição no jogo, caso o passaro bata em algum cano o estado do jogo sera 2 que quer dizer que o jogador morreu, ele deve tocar na tela para reiniciar o jogo
	private  void detectarColisoes()
	{
		circuloPassaro.set(
				50 + posicaoHorizontalPassaro + passaros[0].getWidth() /2,
				posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2,
				passaros[0].getWidth() / 2);

		Pao.set(50+ posicaoPaoHorizontal, posicaoPaoVertical, ouro.getWidth());

		Pao.set(50+ posicaoPaoHorizontal ,posicaoPaoVertical, prata.getWidth());


		retanguloCanoBaixo.set(
				posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);

		retanguloCanoCima.set(
				posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
				canoTope.getWidth(), canoTope.getHeight()
		);

		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);

		if (colidiuCanoCima || colidiuCanoBaixo)
		{
			if (estadoJogo == 1)
			{
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}
	//Esta desenhando e renderizando as imagens na tela como o passaro,tela de fundo,os canos e a tela de game over
	private void desenharTexturas()
	{
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(fundo,0,0,larguraDispositivo, alturaDispositivo);
		batch.draw( passaros[ (int) variacao],
				50 + posicaoHorizontalPassaro,posicaoInicialVerticalPassaro);
		//Moeda ouro
		batch.draw(ouro, 50+ posicaoPaoHorizontal ,posicaoPaoVertical);
		//Moeda Prata
		batch.draw(prata, 50 +posicaoPaoHorizontal, posicaoPaoVertical);
		//cano baixo
		batch.draw(canoBaixo, posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical);
		batch.draw(canoTope, posicaoCanoHorizontal,
				alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo/2,
				alturaDispositivo -110);
		//teste para logo
		if(estadoJogo == 0)
		{
			batch.draw(Logo, larguraDispositivo / 2 - Logo.getWidth()/2,
					alturaDispositivo / 2);
		}

		if (estadoJogo == 2)
		{
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth()/2,
					alturaDispositivo / 2);
			textoReiniciar.draw(batch,
					"Toque para reiniciar!", larguraDispositivo/2 -140,
					alturaDispositivo/2 - gameOver.getHeight()/2 );
			textoMelhorPontuacao.draw(batch,
					"Seu record é: "+ pontuacaoMaxima +" pontos",
					larguraDispositivo/2 -140, alturaDispositivo/2 - gameOver.getHeight());
		}
		batch.end();
	}
	//esta vendo se o jogador passo entre os canos, caso ele tenha passado marca um ponto
	public  void  validarPontos()
	{
		if( posicaoCanoHorizontal < 50-passaros[0].getWidth() )
		{
			if(!passouCano)
			{
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}


		variacao += Gdx.graphics.getDeltaTime() * 10;

		if(variacao > 3)
		{
			variacao = 0;
		}
	}
	//esta ajustando a viewport do jogo
	@Override
	public  void  resize(int width, int height){
		viewport.update(width, height);
	}

	//Ele é usado para liberar os recursos do jogo, quando o jogador sai do jogo
	@Override
	public void dispose () {

	}



}
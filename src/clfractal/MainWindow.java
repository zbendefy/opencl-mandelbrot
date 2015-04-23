package clfractal;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import clframework.common.CLUtils;

public class MainWindow extends javax.swing.JFrame implements ActionListener,
		ChangeListener {

	private JFrame mainFrame;
	private ImagePanel imagePanel;
	private JLabel lblPlatform;
	private JLabel lblDevice;
	private JLabel lblIter;
	private JLabel lblInfobar;
	private JComboBox<String> comboPlatformList;
	private JComboBox<String> comboDeviceList;
	private JButton btnMoveLeft, btnMoveRight, btnMoveUp, btnMoveDn, btnZoomIn,
			btnZoomOut;
	private JSlider sliderIterationLevel;
	private JCheckBox checkGlsharing, checkHighPrecision;

	private static final String iterTxt = "Iterations: ";
	private static final String version = "1.0";

	private final float moveFactor = 0.2f;
	private final float zoomFactor = 1.4f;

	private FractalCalc fractalCalc;

	private void updateInfobar() {
		if (lblInfobar != null && fractalCalc != null) {
			String devicename = CLUtils.GetDeviceName(
					comboPlatformList.getSelectedIndex(),
					comboDeviceList.getSelectedIndex());
			lblInfobar.setText("Pos: (" + fractalCalc.getPosx() + ", "
					+ fractalCalc.getPosy() + ") | Zoom: "
					+ fractalCalc.getZoom() + " | Device: " + devicename
					+ " | Execution time (ms): "
					+ fractalCalc.getLastExecTime());
		}
	}

	private int getSliderValue(int rawVal) {
		return rawVal * rawVal;
	}

	MainWindow() {

		mainFrame = new JFrame("OpenCL Mandelbrot viewer" + version);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout layout = new SpringLayout();
		mainFrame.setMinimumSize(new Dimension(540, 200));
		mainFrame.setLayout(layout);
		Container contentPane = mainFrame.getContentPane();

		final int LabelWidth = 80;
		final int comboDist = 385;

		String[] pllist = CLUtils.GetCLPlatformNames();
		comboPlatformList = new JComboBox<String>(pllist);
		comboPlatformList.addActionListener(this);
		mainFrame.add(comboPlatformList);
		layout.putConstraint(SpringLayout.NORTH, comboPlatformList, 10,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, comboPlatformList, LabelWidth,
				SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, comboPlatformList, -comboDist,
				SpringLayout.EAST, contentPane);

		String[] dlist = CLUtils.GetCLDeviceNames(0);
		comboDeviceList = new JComboBox<String>(dlist);
		comboDeviceList.addActionListener(this);
		mainFrame.add(comboDeviceList);
		layout.putConstraint(SpringLayout.NORTH, comboDeviceList, 10,
				SpringLayout.SOUTH, comboPlatformList);
		// layout.putConstraint(SpringLayout.SOUTH, devicelist, 80,
		// SpringLayout.SOUTH, platformlist);
		layout.putConstraint(SpringLayout.WEST, comboDeviceList, LabelWidth,
				SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, comboDeviceList, -comboDist,
				SpringLayout.EAST, contentPane);

		lblPlatform = new JLabel("Platforms:");
		lblPlatform.setToolTipText("Platforms");
		mainFrame.add(lblPlatform);
		layout.putConstraint(SpringLayout.NORTH, lblPlatform, 3,
				SpringLayout.NORTH, comboPlatformList);
		layout.putConstraint(SpringLayout.WEST, lblPlatform, 5,
				SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, lblPlatform, -5,
				SpringLayout.WEST, comboPlatformList);

		lblDevice = new JLabel("Devices:");
		lblDevice.setToolTipText("Devices");
		mainFrame.add(lblDevice);
		layout.putConstraint(SpringLayout.NORTH, lblDevice, 3,
				SpringLayout.NORTH, comboDeviceList);
		layout.putConstraint(SpringLayout.WEST, lblDevice, 5,
				SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, lblDevice, -5,
				SpringLayout.WEST, comboDeviceList);

		imagePanel = new ImagePanel();
		mainFrame.add(imagePanel);
		layout.putConstraint(SpringLayout.NORTH, imagePanel, 10,
				SpringLayout.SOUTH, comboDeviceList);
		layout.putConstraint(SpringLayout.WEST, imagePanel, 5,
				SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, imagePanel, -5,
				SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, imagePanel, -18,
				SpringLayout.SOUTH, contentPane);
		imagePanel.setFocusable(true);
		imagePanel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				imagePanel.requestFocus();
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		btnMoveLeft = new JButton("<");
		btnMoveLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fractalCalc != null) {
					fractalCalc.modPosx(-moveFactor);
				}
				RedrawView();
			}
		});
		mainFrame.add(btnMoveLeft);
		layout.putConstraint(SpringLayout.NORTH, btnMoveLeft, 30,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, btnMoveLeft, 50,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, btnMoveLeft, -100,
				SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, btnMoveLeft, -55,
				SpringLayout.EAST, contentPane);

		btnMoveRight = new JButton(">");
		btnMoveRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fractalCalc != null) {
					fractalCalc.modPosx(moveFactor);
				}
				RedrawView();
			}
		});
		mainFrame.add(btnMoveRight);
		layout.putConstraint(SpringLayout.NORTH, btnMoveRight, 30,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, btnMoveRight, 50,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, btnMoveRight, -50,
				SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, btnMoveRight, -5,
				SpringLayout.EAST, contentPane);

		btnMoveUp = new JButton("^");
		btnMoveUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fractalCalc != null) {
					fractalCalc.modPosy(-moveFactor);
				}
				RedrawView();
			}
		});
		mainFrame.add(btnMoveUp);
		layout.putConstraint(SpringLayout.NORTH, btnMoveUp, 5,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, btnMoveUp, 25,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, btnMoveUp, -75,
				SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, btnMoveUp, -30,
				SpringLayout.EAST, contentPane);

		btnMoveDn = new JButton("v");
		btnMoveDn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fractalCalc != null) {
					fractalCalc.modPosy(moveFactor);
				}
				RedrawView();
			}
		});
		mainFrame.add(btnMoveDn);
		layout.putConstraint(SpringLayout.NORTH, btnMoveDn, 55,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, btnMoveDn, 75,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, btnMoveDn, -75,
				SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, btnMoveDn, -30,
				SpringLayout.EAST, contentPane);

		btnZoomIn = new JButton("+");
		btnZoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fractalCalc != null) {
					fractalCalc.setZoom(fractalCalc.getZoom() / zoomFactor);
				}
				RedrawView();
			}
		});
		mainFrame.add(btnZoomIn);
		layout.putConstraint(SpringLayout.NORTH, btnZoomIn, 5,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, btnZoomIn, 35,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, btnZoomIn, -150,
				SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, btnZoomIn, -105,
				SpringLayout.EAST, contentPane);

		btnZoomOut = new JButton("-");
		btnZoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fractalCalc != null) {
					fractalCalc.setZoom(fractalCalc.getZoom() * zoomFactor);
				}
				RedrawView();
			}
		});
		mainFrame.add(btnZoomOut);
		layout.putConstraint(SpringLayout.NORTH, btnZoomOut, 45,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, btnZoomOut, 75,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, btnZoomOut, -150,
				SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, btnZoomOut, -105,
				SpringLayout.EAST, contentPane);

		sliderIterationLevel = new JSlider(0, 1, 60, 5);
		mainFrame.add(sliderIterationLevel);
		sliderIterationLevel.addChangeListener((ChangeListener) this);
		layout.putConstraint(SpringLayout.NORTH, sliderIterationLevel, 55,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, sliderIterationLevel, 75,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, sliderIterationLevel, -380,
				SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, sliderIterationLevel, -150,
				SpringLayout.EAST, contentPane);

		lblIter = new JLabel(
				iterTxt
						+ String.valueOf(getSliderValue(sliderIterationLevel
								.getValue())));
		mainFrame.add(lblIter);
		layout.putConstraint(SpringLayout.NORTH, lblIter, -20,
				SpringLayout.NORTH, sliderIterationLevel);
		layout.putConstraint(SpringLayout.SOUTH, lblIter, -2,
				SpringLayout.NORTH, sliderIterationLevel);
		layout.putConstraint(SpringLayout.WEST, lblIter, 5, SpringLayout.WEST,
				sliderIterationLevel);
		layout.putConstraint(SpringLayout.EAST, lblIter, 0, SpringLayout.EAST,
				sliderIterationLevel);

		checkHighPrecision = new JCheckBox("64-bit");
		checkHighPrecision.addActionListener(this);
		mainFrame.add(checkHighPrecision);
		layout.putConstraint(SpringLayout.NORTH, checkHighPrecision, 5,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, checkHighPrecision, -3,
				SpringLayout.NORTH, lblIter);
		layout.putConstraint(SpringLayout.WEST, checkHighPrecision, 0,
				SpringLayout.WEST, sliderIterationLevel);
		layout.putConstraint(SpringLayout.EAST, checkHighPrecision, 90,
				SpringLayout.WEST, sliderIterationLevel);

		checkGlsharing = new JCheckBox("GL Sharing");
		checkGlsharing.addActionListener(this);
		mainFrame.add(checkGlsharing);
		layout.putConstraint(SpringLayout.NORTH, checkGlsharing, 5,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, checkGlsharing, -3,
				SpringLayout.NORTH, lblIter);
		layout.putConstraint(SpringLayout.WEST, checkGlsharing, 5,
				SpringLayout.EAST, checkHighPrecision);
		layout.putConstraint(SpringLayout.EAST, checkGlsharing, 100,
				SpringLayout.EAST, checkHighPrecision);
		checkGlsharing.setVisible(false);

		lblInfobar = new JLabel("Loading...");
		mainFrame.add(lblInfobar);
		layout.putConstraint(SpringLayout.NORTH, lblInfobar, -17,
				SpringLayout.SOUTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, lblInfobar, 0,
				SpringLayout.SOUTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, lblInfobar, 5,
				SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, lblInfobar, -5,
				SpringLayout.EAST, contentPane);

		mainFrame.pack();
		mainFrame.setVisible(true);
		mainFrame.setSize(780, 500);
		mainFrame.addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent evt) {
				RedrawView();
			}

			@Override
			public void componentHidden(ComponentEvent arg0) {
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
			}

			@Override
			public void componentShown(ComponentEvent arg0) {
			}
		});

		imagePanel.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (fractalCalc != null) {
					if (e.getKeyCode() == KeyEvent.VK_LEFT) {
						fractalCalc.modPosx(-moveFactor * 0.1f);
						RedrawView();
					} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
						fractalCalc.modPosx(moveFactor * 0.1f);
						RedrawView();
					} else if (e.getKeyCode() == KeyEvent.VK_UP) {
						fractalCalc.modPosy(-moveFactor * 0.1f);
						RedrawView();
					} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
						fractalCalc.modPosy(moveFactor * 0.1f);
						RedrawView();
					} else if (e.getKeyCode() == KeyEvent.VK_PLUS
							|| e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
						fractalCalc.setZoom(fractalCalc.getZoom() / zoomFactor);
						RedrawView();
					} else if (e.getKeyCode() == KeyEvent.VK_MINUS
							|| e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
						fractalCalc.setZoom(fractalCalc.getZoom() * zoomFactor);
						RedrawView();
					}
				}
			}
		});

		populateDeviceList();
		recreateCalculator(0, 0);
		checkHWSupport(0, 0);
		RedrawView();
		imagePanel.requestFocus();
	}

	private void recreateCalculator(int pid, int did) {
		if (!CLUtils.isValidDevice(pid, did)) {
			lblInfobar.setText("No installed OpenCL devices found!");
			return;
		}

		double posx = 0, posy = 0, zoom = 2.0f;

		if (fractalCalc != null) {
			if (fractalCalc.getPlatformid() == pid
					&& fractalCalc.getDeviceid() == did) {
				return;
			}

			posx = fractalCalc.getPosx();
			posy = fractalCalc.getPosy();
			zoom = fractalCalc.getZoom();
			fractalCalc.deleteResources();
			fractalCalc = null;
		}
		checkHighPrecision.setSelected(false);

		try {
			fractalCalc = new FractalCalc(imagePanel, pid, did);
			fractalCalc.setPosx(posx);
			fractalCalc.setPosy(posy);
			fractalCalc.setZoom(zoom);
			fractalCalc.setIterations(getSliderValue(sliderIterationLevel
					.getValue()));
		} catch (Exception e) {
			fractalCalc = null;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(comboPlatformList)) {
			recreateCalculator(comboPlatformList.getSelectedIndex(), 0);
			populateDeviceList();
			checkHWSupport(comboPlatformList.getSelectedIndex(), 0);
			RedrawView();
		}
		if (e.getSource().equals(comboDeviceList)) {
			recreateCalculator(comboPlatformList.getSelectedIndex(),
					comboDeviceList.getSelectedIndex());
			checkHWSupport(comboPlatformList.getSelectedIndex(),
					comboDeviceList.getSelectedIndex());
			RedrawView();
		}
		if (e.getSource().equals(checkHighPrecision)) {
			fractalCalc.setHighPrecision(checkHighPrecision.isSelected());
			RedrawView();
		}
		if (e.getSource().equals(checkGlsharing)) {
			fractalCalc.setGLSharing(checkGlsharing.isSelected());
			RedrawView();
		}
	}

	private void checkHWSupport(int pid, int did) {
		boolean _64bitsupport = CLUtils.isExtSupported(pid, did, "cl_khr_fp64");
		if (!_64bitsupport)
			checkHighPrecision.setSelected(false);
		checkHighPrecision.setEnabled(_64bitsupport);
	}

	private void populateDeviceList() {
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		int selected = comboPlatformList.getSelectedIndex();
		if (selected != -1) {
			String[] devices = CLUtils.GetCLDeviceDetails(selected);
			for (int i = 0; i < devices.length; i++) {
				m.addElement(devices[i].trim());
			}
			comboDeviceList.setModel(m);
		}
	}

	public void RedrawView() {
		if (fractalCalc != null) {
			try {
				fractalCalc.updateImage();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
		updateInfobar();
	}

	@Override
	public void stateChanged(ChangeEvent arg) {

		if (arg.getSource() == sliderIterationLevel && fractalCalc != null) {
			fractalCalc.setIterations(getSliderValue(sliderIterationLevel
					.getValue()));
			lblIter.setText(iterTxt
					+ String.valueOf(getSliderValue(sliderIterationLevel
							.getValue())));
			RedrawView();
		}
	}

}
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

import clfractal.FractalCalc.FractalModes;
import clframework.common.CLDevice;

public class MainWindow extends javax.swing.JFrame implements ActionListener, ChangeListener, IMouseEvents {

	private JFrame mainFrame;
	private ImagePanel imagePanel;
	private JLabel lblPlatform;
	private JLabel lblDevice;
	private JLabel lblIter;
	private JLabel lblInfobar;
	private JLabel lblExponent;
	private JComboBox<String> comboPlatformList;
	private JComboBox<String> comboDeviceList;
	private JComboBox<Integer> comboFractalType;
	private JButton btnMoveLeft, btnMoveRight, btnMoveUp, btnMoveDn, btnZoomIn, btnZoomOut;
	private JSlider sliderIterationLevel;
	private JCheckBox switchFractalMode, checkHighPrecision;

	private static final String iterTxt = "Iterations: ";
	private static final String version = "1.3d";

	private final double moveFactor = 0.2;
	private final double zoomFactor = 1.4;

	private FractalCalc fractalCalc;

	private void updateInfobar() {
		if (lblInfobar != null && fractalCalc != null) {
			lblInfobar.setText("Pos: (" + fractalCalc.getPosx() + ", " + fractalCalc.getPosy() + ") | Zoom: "
					+ fractalCalc.getZoom() + " | Screen: " + imagePanel.getWidth() + "x" + imagePanel.getHeight()
					+ " | Execution time (ms): " + fractalCalc.getLastExecTime());
		}
	}

	private int getSliderValue(int rawVal) {
		return rawVal * rawVal;
	}

	MainWindow() {

		mainFrame = new JFrame("OpenCL Mandelbrot viewer " + version);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout layout = new SpringLayout();
		mainFrame.setMinimumSize(new Dimension(590, 200));
		mainFrame.setLayout(layout);
		Container contentPane = mainFrame.getContentPane();

		final int LabelWidth = 80;
		final int comboDist = 450;

		String[] pllist = CLDevice.GetCLPlatformNames();
		comboPlatformList = new JComboBox<String>(pllist);
		comboPlatformList.addActionListener(this);
		mainFrame.add(comboPlatformList);
		layout.putConstraint(SpringLayout.NORTH, comboPlatformList, 10, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, comboPlatformList, LabelWidth, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, comboPlatformList, -comboDist, SpringLayout.EAST, contentPane);

		String[] dlist = CLDevice.GetCLDeviceNames(0);
		comboDeviceList = new JComboBox<String>(dlist);
		comboDeviceList.addActionListener(this);
		mainFrame.add(comboDeviceList);
		layout.putConstraint(SpringLayout.NORTH, comboDeviceList, 10, SpringLayout.SOUTH, comboPlatformList);
		// layout.putConstraint(SpringLayout.SOUTH, devicelist, 80,
		// SpringLayout.SOUTH, platformlist);
		layout.putConstraint(SpringLayout.WEST, comboDeviceList, LabelWidth, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, comboDeviceList, -comboDist, SpringLayout.EAST, contentPane);

		lblPlatform = new JLabel("Platforms:");
		lblPlatform.setToolTipText("Platforms");
		mainFrame.add(lblPlatform);
		layout.putConstraint(SpringLayout.NORTH, lblPlatform, 3, SpringLayout.NORTH, comboPlatformList);
		layout.putConstraint(SpringLayout.WEST, lblPlatform, 5, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, lblPlatform, -5, SpringLayout.WEST, comboPlatformList);

		lblDevice = new JLabel("Devices:");
		lblDevice.setToolTipText("Devices");
		mainFrame.add(lblDevice);
		layout.putConstraint(SpringLayout.NORTH, lblDevice, 3, SpringLayout.NORTH, comboDeviceList);
		layout.putConstraint(SpringLayout.WEST, lblDevice, 5, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, lblDevice, -5, SpringLayout.WEST, comboDeviceList);

		imagePanel = new ImagePanel();
		imagePanel.SetMouseListener(this);
		mainFrame.add(imagePanel);
		layout.putConstraint(SpringLayout.NORTH, imagePanel, 10, SpringLayout.SOUTH, comboDeviceList);
		layout.putConstraint(SpringLayout.WEST, imagePanel, 5, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, imagePanel, -5, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, imagePanel, -18, SpringLayout.SOUTH, contentPane);
		imagePanel.setFocusable(true);
		imagePanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				imagePanel.requestFocus();
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
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
		layout.putConstraint(SpringLayout.NORTH, btnMoveLeft, 30, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, btnMoveLeft, 50, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, btnMoveLeft, -100, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, btnMoveLeft, -55, SpringLayout.EAST, contentPane);

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
		layout.putConstraint(SpringLayout.NORTH, btnMoveRight, 30, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, btnMoveRight, 50, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, btnMoveRight, -50, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, btnMoveRight, -5, SpringLayout.EAST, contentPane);

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
		layout.putConstraint(SpringLayout.NORTH, btnMoveUp, 5, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, btnMoveUp, 25, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, btnMoveUp, -75, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, btnMoveUp, -30, SpringLayout.EAST, contentPane);

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
		layout.putConstraint(SpringLayout.NORTH, btnMoveDn, 55, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, btnMoveDn, 75, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, btnMoveDn, -75, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, btnMoveDn, -30, SpringLayout.EAST, contentPane);

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
		layout.putConstraint(SpringLayout.NORTH, btnZoomIn, 5, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, btnZoomIn, 25, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, btnZoomIn, -130, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, btnZoomIn, -5, SpringLayout.WEST, btnMoveUp);

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
		layout.putConstraint(SpringLayout.NORTH, btnZoomOut, 55, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, btnZoomOut, 75, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, btnZoomOut, -130, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, btnZoomOut, -5, SpringLayout.WEST, btnMoveUp);

		sliderIterationLevel = new JSlider(0, 1, 60, 7);
		mainFrame.add(sliderIterationLevel);
		sliderIterationLevel.addChangeListener((ChangeListener) this);
		layout.putConstraint(SpringLayout.NORTH, sliderIterationLevel, 55, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, sliderIterationLevel, 75, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, sliderIterationLevel, -comboDist + 5, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, sliderIterationLevel, -10, SpringLayout.WEST, btnZoomIn);

		lblIter = new JLabel(iterTxt + String.valueOf(getSliderValue(sliderIterationLevel.getValue())));
		mainFrame.add(lblIter);
		layout.putConstraint(SpringLayout.NORTH, lblIter, -20, SpringLayout.NORTH, sliderIterationLevel);
		layout.putConstraint(SpringLayout.SOUTH, lblIter, -2, SpringLayout.NORTH, sliderIterationLevel);
		layout.putConstraint(SpringLayout.WEST, lblIter, 5, SpringLayout.WEST, sliderIterationLevel);
		layout.putConstraint(SpringLayout.EAST, lblIter, 0, SpringLayout.EAST, sliderIterationLevel);

		checkHighPrecision = new JCheckBox("64-bit");
		checkHighPrecision.addActionListener(this);
		mainFrame.add(checkHighPrecision);
		layout.putConstraint(SpringLayout.NORTH, checkHighPrecision, 5, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, checkHighPrecision, -3, SpringLayout.NORTH, lblIter);
		layout.putConstraint(SpringLayout.WEST, checkHighPrecision, 0, SpringLayout.WEST, sliderIterationLevel);
		layout.putConstraint(SpringLayout.EAST, checkHighPrecision, 75, SpringLayout.WEST, sliderIterationLevel);

		switchFractalMode = new JCheckBox("Julia mode");
		switchFractalMode.addActionListener(this);
		mainFrame.add(switchFractalMode);
		layout.putConstraint(SpringLayout.NORTH, switchFractalMode, 5, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, switchFractalMode, -3, SpringLayout.NORTH, lblIter);
		layout.putConstraint(SpringLayout.WEST, switchFractalMode, 5, SpringLayout.EAST, checkHighPrecision);
		layout.putConstraint(SpringLayout.EAST, switchFractalMode, 100, SpringLayout.EAST, checkHighPrecision);

		lblExponent = new JLabel("Exponent:");
		mainFrame.add(lblExponent);
		layout.putConstraint(SpringLayout.NORTH, lblExponent, 5, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, lblExponent, -3, SpringLayout.NORTH, lblIter);
		layout.putConstraint(SpringLayout.WEST, lblExponent, 10, SpringLayout.EAST, switchFractalMode);
		layout.putConstraint(SpringLayout.EAST, lblExponent, -60, SpringLayout.WEST, btnZoomIn);

		comboFractalType = new JComboBox<Integer>();
		comboFractalType.addActionListener(this);
		mainFrame.add(comboFractalType);
		layout.putConstraint(SpringLayout.NORTH, comboFractalType, 5, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, comboFractalType, -3, SpringLayout.NORTH, lblIter);
		layout.putConstraint(SpringLayout.WEST, comboFractalType, -10, SpringLayout.EAST, lblExponent);
		layout.putConstraint(SpringLayout.EAST, comboFractalType, -10, SpringLayout.WEST, btnZoomIn);
		populateFractalModesList();

		lblInfobar = new JLabel("Loading...");
		mainFrame.add(lblInfobar);
		layout.putConstraint(SpringLayout.NORTH, lblInfobar, -17, SpringLayout.SOUTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, lblInfobar, 0, SpringLayout.SOUTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, lblInfobar, 5, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, lblInfobar, -5, SpringLayout.EAST, contentPane);

		mainFrame.pack();
		mainFrame.setVisible(true);
		mainFrame.setSize(780, 500);
		mainFrame.addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent evt) {
				if (fractalCalc != null)
					fractalCalc.onResize(imagePanel.getWidth(), imagePanel.getHeight());
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
			}

			@Override
			public void keyReleased(KeyEvent e) {
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
					} else if (e.getKeyCode() == KeyEvent.VK_ADD || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
						fractalCalc.setZoom(fractalCalc.getZoom() / zoomFactor);
						RedrawView();
					} else if (e.getKeyCode() == KeyEvent.VK_SUBTRACT || e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
						fractalCalc.setZoom(fractalCalc.getZoom() * zoomFactor);
						RedrawView();
					}
				}
			}
		});

		populateDeviceList();
		recreateCalculator(0, 0);
		RedrawView();
		imagePanel.requestFocus();
	}

	private void recreateCalculator(int pid, int did) {
		if (fractalCalc != null) {
			if (fractalCalc.getDevice().getPlatformid() == pid && fractalCalc.getDevice().getDeviceid() == did) {
				return;
			}
		}

		CLDevice device = null;
		try {
			device = new CLDevice(pid, did);
		} catch (Exception e) {
			lblInfobar.setText("No installed OpenCL devices found!");
			return;
		}

		double[] state = null;

		if (fractalCalc != null) {
			state = fractalCalc.getState();
			fractalCalc.deleteResources();
			fractalCalc = null;
		}

		checkHighPrecision.setSelected(false);

		try {
			fractalCalc = new FractalCalc(imagePanel, device);
			fractalCalc.onResize(imagePanel.getWidth(), imagePanel.getHeight());
			checkHWSupport();

			if (state != null) {
				fractalCalc.restoreState(state);
			}
			fractalCalc.setIterations(getSliderValue(sliderIterationLevel.getValue()));
		} catch (Exception e) {
			fractalCalc = null;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(comboPlatformList)) {
			recreateCalculator(comboPlatformList.getSelectedIndex(), 0);
			populateDeviceList();
			RedrawView();
		}
		if (e.getSource().equals(comboDeviceList)) {
			recreateCalculator(comboPlatformList.getSelectedIndex(), comboDeviceList.getSelectedIndex());
			RedrawView();
		}
		if (e.getSource().equals(checkHighPrecision)) {
			if (fractalCalc != null) {
				fractalCalc.setHighPrecision(checkHighPrecision.isSelected());
				RedrawView();
			}
		}
		if (e.getSource().equals(switchFractalMode)) {
			if (fractalCalc != null) {
				fractalCalc.switchMode(switchFractalMode.isSelected() ? FractalModes.JULIA : FractalModes.MANDELBROT);
				RedrawView();
			}
		}
		if (e.getSource().equals(comboFractalType)) {
			if (fractalCalc != null) {
				fractalCalc.setExponent((int) Integer.parseInt(comboFractalType.getSelectedItem().toString()));
				RedrawView();
			}
		}
	}

	private void checkHWSupport() {
		if (fractalCalc == null)
			return;

		boolean _64bitsupport = fractalCalc.getDevice().isExtSupported("cl_khr_fp64");
		if (!_64bitsupport)
			checkHighPrecision.setSelected(false);
		checkHighPrecision.setEnabled(_64bitsupport);
	}

	private void populateDeviceList() {
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		int selected = comboPlatformList.getSelectedIndex();
		if (selected != -1) {
			String[] devices = CLDevice.GetCLDeviceNames(selected);
			for (int i = 0; i < devices.length; i++) {
				m.addElement(devices[i].trim());
			}
			comboDeviceList.setModel(m);
		}
	}

	private void populateFractalModesList() {
		DefaultComboBoxModel<Integer> m = new DefaultComboBoxModel<Integer>();
		int beginIndex = 0;
		for (int i = 2; i <= 16; i++) {
			m.addElement(i);
		}
		comboFractalType.setModel(m);
		comboFractalType.setSelectedIndex(beginIndex);
	}

	public void RedrawView() {
		if (fractalCalc != null) {
			try {
				imagePanel.updateImageSize();
				fractalCalc.drawImage(imagePanel.getImageByteArray());
				imagePanel.repaint();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
		updateInfobar();
	}

	@Override
	public void stateChanged(ChangeEvent arg) {

		if (arg.getSource() == sliderIterationLevel && fractalCalc != null) {
			fractalCalc.setIterations(getSliderValue(sliderIterationLevel.getValue()));
			lblIter.setText(iterTxt + String.valueOf(getSliderValue(sliderIterationLevel.getValue())));
			RedrawView();
		}
	}

	@Override
	public void OnDragView(int x, int y, int btn) {
		if (fractalCalc != null) {
            if ((btn >= 2) && fractalCalc.getFractalMode() != FractalModes.MANDELBROT) {
                fractalCalc.modJuliaPosx((float) x * -0.0002f);
                fractalCalc.modJuliaPosy((float) y * -0.0002f);
            } else {
                fractalCalc.modPosx((float) x * -0.001f);
                fractalCalc.modPosy((float) y * -0.001f);
            }
			RedrawView();
		}
	}

	@Override
	public void OnWheelEvent(int wheelRotation) {
		if (fractalCalc != null) {
			if (wheelRotation > 0)
				fractalCalc.setZoom(fractalCalc.getZoom() * zoomFactor);
			else
				fractalCalc.setZoom(fractalCalc.getZoom() / zoomFactor);
			RedrawView();
		}
	}

}
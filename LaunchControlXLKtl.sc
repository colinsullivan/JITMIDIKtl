LaunchControlXLKtl : MIDIKtl {  // put your Ktl name here
	classvar <>verbose = false;   // debugging flag
	var <>softWithin = 0.05;    // will use softSet, so have a tweakable limit for it
	var <lastVals;        // remember the last values for better control
	//var <valRange, <minval, <range;  // some MIDI controllers may not reach full 0-127 range, 
	// provide a global range for that.
	// (if the range is different for each controller, 
	// make a dict of all ranges.)


	init { 
		super.init;    // get the ctlNames created for MyOwnKtl
		ctlNames = defaults[this.class];
		orderedCtlNames = ctlNames.keys.asArray.sort;  // a sorted list of the names
		lastVals = ();  // initialise lastVals
		//this.valRange = [0, 127];  // set default valRange; 
	}

	// makeDefaults puts ctlNames for this device/class 
	// into MIDIKtl.defaults.
	*makeDefaults { 

		// just one bank of sliders
    defaults.put(this, (

      // general controls that are not associated with a scene
      0: (),
			1: (        // clear names for the elements;
				// '0_7' is midi chan 0, cc number 7, 
				// combined into a symbol for speed. 
        knu1: '0_13', knu2: '0_14', knu3: '0_15', knu4: '0_16', knu5: '0_17', knu6: '0_18', knu7: '0_19', knu8: '0_20',
        knm1: '0_29', knm2: '0_30', knm3: '0_31', knm4: '0_32', knm5: '0_33', knm6: '0_34', knm7: '0_35', knm8: '0_36',
        knl1: '0_49', knl2: '0_50', knl5: '0_51', knl4: '0_52', knl5: '0_55', knl6: '0_54', knl7: '0_55', knl8: '0_56',

        'sl1': '0_77', 'sl2': '0_78', 'sl3': '0_79', 'sl4': '0_80', 'sl5': '0_81', 'sl6': '0_82', 'sl7': '0_83', 'sl8': '0_84'
			)
    )
		);
	}

	// a method for registering actions based on ctl name. 

	//mapCC { |ctl= \sl1, action| 
		//var ccDictKey = ctlNames[ctl]; // e.g. '0_42'
		//if (ccDictKey.isNil) { 
			//warn("key % : no chan_ccnum found!\n".format(ctl));
		//} { 
			//ccDict.put(ccDictKey, action);
		//}
	//}

	// methods to map to various JITGuis: 
	// EnvirGui, TdefGui, PdefGui, NdefGui, and ProxyMixer. 
	// these have a number of design decisions based on  
	// how this device can work best for these kinds of guis.

	// assume this has only a limited number of items (sliders)

	//mapToEnvirGui { |gui, indices| 
		//var elementKeys; 

		//// which slider keys to be used for the gui?
		//indices = indices ? (1..8); 

		//elementKeys = orderedCtlNames[indices - 1]; 

		//// for each slider, 
		//// get the editKey, the sliders lastVal, 
		//// and do use softSet for setting the proxy's param. 
		////
		//elementKeys.do { |key, i|    
			//this.mapCC(key, 
				//{ |ccval| 
					//var envir = gui.envir;
					//var parKey =  gui.editKeys[i];
					//var normVal = this.norm(ccval);
					//var lastVal = lastVals[key];
					//if (envir.notNil and: { parKey.notNil } ) { 
						//envir.softSet(parKey, normVal, softWithin, false, lastVal, gui.getSpec(parKey))
					//};
					//// remember lastVal for the next time
					//lastVals.put(key, normVal) ;
				//}
			//)
		//};
	//}
	//// for PdefGui and TdefGui, just map to their EnvirGui.
	//mapToPdefGui { |gui, indices| 
		//this.mapToEnvirGui(gui.envirGui, indices);
	//}

	//mapToTdefGui { |gui, indices| 
		//this.mapToEnvirGui(gui.envirGui, indices);
	//}

	//// NdefGui: 
	//mapToNdefGui { |gui, indices, lastIsVol = true| 
		//var elementKeys, lastKey; 
		//indices = indices ? (1..8); 

		//elementKeys = orderedCtlNames[indices - 1].postcs; 


		///// last slider can optionally be a volume control
		//if (lastIsVol) {   
			//lastKey = elementKeys.pop;
			//indices.pop;

			//// use last slider for proxy volume
			//this.mapCC(lastKey, { |ccval| 
				//var lastVal = lastVals[lastKey];
				//var mappedVol = \amp.asSpec.map(this.norm(ccval));
				//var proxy = gui.proxy;
				//if (proxy.notNil) { proxy.softVol_(mappedVol, softWithin, lastVal: lastVal) };
				//lastVals[lastKey] = mappedVol;
			//});
		//};
		//// the other sliders go to the paramGui and will set those
		//this.mapToEnvirGui(gui.paramGui, indices);
	//}


	//// ProxyMixer is more complex: 
	//// here it is again assumed that the mixer has no more than the number of 
	//// available sliders. if you scoll on the mixer, 
	//// they are still reachable.

	//mapToMixer { |mixer, numVols = 8, indices, lastEdIsVol = true, lastIsMaster = true| 

		//var server = mixer.proxyspace.server;
		//var   elementKeys, lastKey, spec;

		//indices = indices ? (1..16); 
		//elementKeys = orderedCtlNames[indices - 1]; 

		//// add master volume on slider 16
		//if (lastIsMaster) { 
			//lastKey = elementKeys.pop; 
			//spec = Spec.add(\mastaVol, [server.volume.min, server.volume.max, \db]);
			//// this.mapCC(lastKey, Volume.softMasterVol(0.05, server, \midi.asSpec));
			//this.mapCC(lastKey, { |ccval| server.volume.volume_(spec.map(this.norm(ccval))) });
		//};      

		//// map first n sliders to proxy volumes
		//elementKeys.keep(numVols).do { |key, i| 
			//this.mapCC(key, 
				//{ |ccval| 
					//var proxy = mixer.arGuis[i].proxy; 
					//var lastVal, mappedVal, lastVol;
					//var spec = \amp.asSpec;
					//if (proxy.notNil) { 
						//lastVal = lastVals[key]; 
						//mappedVal = spec.map(this.norm(ccval)); 
						//lastVol = if (lastVal.notNil) { spec.asSpec.map(lastVal) }; 
						//proxy.softVol_(spec.map(mappedVal), softWithin, true, lastVol ); 
					//};
					////  [key, proxy.key, mappedVal].postcs;
					//lastVals[key] =  mappedVal;
				//};
			//)
		//};
		//// map the rest of the sliders to the NdefGui! 
		//this.mapToNdefGui(mixer.editGui, (numVols + 1 .. elementKeys.size), lastEdIsVol);
	//}
}


var params = Spry.Utils.getLocationParamsAsObject();

var qsParm = new Array();

qsParm['ProcessingChromatoPanel'] = null;
qsParm['GettingStartedPanel'] = null;
qsParm['ChromatogramViewerPanel'] = null;
qsParm['PreparingDataPanel'] = null;
qsParm['CreditPanel'] = null;
qsParm['HelpPanel'] = null;
qsParm['OtherFeaturesPanel'] = null;
qsParm['GenBankPanel'] = null;
qsParm['TechnicalPanel'] = null;

qs();

function qs() {
	var query = window.location.search.substring(1);
	var parms = query.split('&');
	for (var i=0; i<parms.length; i++) {
		var pos = parms[i].indexOf('=');
		if (pos > 0) {
			var key = parms[i].substring(0,pos);
			var val = parms[i].substring(pos+1);
			qsParm[key] = val;
		}
	}
}

parameterString = function()
{
	var paramString = '';
	if (GettingStartedPanel.isOpen()) {
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'GettingStartedPanel=open';
	}
	if (PreparingDataPanel.isOpen()) {
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'PreparingDataPanel=open';
	}
	if (ProcessingChromatoPanel.isOpen()) {
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'ProcessingChromatoPanel=open';
	}
	if (ChromatogramViewerPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'ChromatogramViewerPanel=open';
	}
	if (CreditPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'CreditPanel=open';
	}
	if (HelpPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'HelpPanel=open';
	}
	if (GenBankPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'GenBankPanel=open';
	}
	if (OtherFeaturesPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'OtherFeaturesPanel=open';
	}
	if (TechnicalPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'TechnicalPanel=open';
	}

	return paramString;
};


pageLink = function(page)
{
	document.location.href = page + parameterString();
};


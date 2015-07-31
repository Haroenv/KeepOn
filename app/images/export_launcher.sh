#/bin/bash
function export {
	inkscape --export-area-page --export-dpi $1 --export-png=$2 $3
	echo
}

infile=ic_launcher.svg
outdir="../src/main/res/mipmap"
dpi=90
outfile=ic_launcher.png


export $dpi $outdir-mdpi/$outfile $infile
export $(echo "scale=2; $dpi*1.5" | bc) $outdir-hdpi/$outfile $infile
export $(echo "scale=2; $dpi*2" | bc) $outdir-xhdpi/$outfile $infile
export $(echo "scale=2; $dpi*3" | bc) $outdir-xxhdpi/$outfile $infile
export $(echo "scale=2; $dpi*4" | bc) $outdir-xxxhdpi/$outfile $infile

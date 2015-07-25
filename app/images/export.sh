#/bin/bash
function export {
	# echo exporting $1 dpi to $2
	inkscape --export-area-page --export-dpi $1 --export-png=$2 $3
	echo
}

if [ "$#" -ne 2 ]; then
	echo "Usage: $0 <svg file> <target file name>"
	exit 0
fi

outdir="../src/main/res/drawable"
dpi=90


export $dpi $outdir-mdpi/$2 $1
export $(echo "scale=2; $dpi*1.5" | bc) $outdir-hdpi/$2 $1
export $(echo "scale=2; $dpi*2" | bc) $outdir-xhdpi/$2 $1
export $(echo "scale=2; $dpi*3" | bc) $outdir-xxhdpi/$2 $1

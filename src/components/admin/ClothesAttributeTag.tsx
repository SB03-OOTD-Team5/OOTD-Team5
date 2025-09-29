import { X } from "lucide-react";

interface ClothesAttributeTagProps {
  label: string;
  showDelete?: boolean;
  onDelete?: () => void;
  variant?: "normal" | "selected";
}

export default function ClothesAttributeTag({ 
  label, 
  showDelete = false, 
  onDelete,
  variant = "normal" 
}: ClothesAttributeTagProps) {
  if (variant === "selected") {
    return (
      <div className="bg-[#f0f9ff] border border-[#99d4ff] rounded-[7px] px-1.5 py-1 flex items-center gap-1">
        <span className="text-[#575765] text-[14px] font-['SUIT:SemiBold',_sans-serif] tracking-[-0.35px]">
          {label}
        </span>
        {showDelete && onDelete && (
          <button
            onClick={onDelete}
            className="flex items-center justify-center size-3 hover:opacity-70"
          >
            <X className="size-3 text-[#575765]" />
          </button>
        )}
      </div>
    );
  }

  return (
    <div className="bg-white border border-[#d4d4d9] rounded-[7px] px-1.5 py-1 flex items-center gap-1">
      <span className="text-[#808089] text-[14px] font-['SUIT:SemiBold',_sans-serif] tracking-[-0.35px] text-center">
        {label}
      </span>
      {showDelete && onDelete && (
        <button
          onClick={onDelete}
          className="flex items-center justify-center size-3 hover:opacity-70"
        >
          <X className="size-3 text-[#808089]" />
        </button>
      )}
    </div>
  );
}
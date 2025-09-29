import { Toaster as Sonner, type ToasterProps } from "sonner"
import { CheckCircle2, XCircle, AlertCircle } from "lucide-react"

const Toaster = ({ ...props }: ToasterProps) => {
  return (
    <Sonner
      theme="light"
      className="toaster group"
      position="bottom-center"
      toastOptions={{
        style: {
          background: 'rgba(33, 33, 38, 0.95)',
          border: 'none',
          color: '#ffffff',
          borderRadius: '12px',
          padding: '16px 20px',
          minWidth: '400px',
          maxWidth: '600px',
          width: 'auto',
          fontSize: '14px',
          fontWeight: '500',
          boxShadow: '0px 8px 32px rgba(0, 0, 0, 0.32)',
        },
        className: 'toast-custom',
      }}
      icons={{
        success: <CheckCircle2 className="size-5 text-green-400" />,
        error: <XCircle className="size-5 text-red-400" />,
        warning: <AlertCircle className="size-5 text-yellow-400" />,
        info: <AlertCircle className="size-5 text-blue-400" />,
      }}
      {...props}
    />
  )
}

export { Toaster }
